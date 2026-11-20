package vn.kma.idp.security;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Private key custody only. Production references are aliases in the configured PKCS12, never request paths. */
@Component
public class SigningKeyMaterial {
    public record Material(RSAKey key,String reference){}
    private final Environment env;private final SecretBox secrets;
    private final Map<String,RSAKey> memory=new ConcurrentHashMap<>();
    public SigningKeyMaterial(Environment env,SecretBox secrets){this.env=env;this.secrets=secrets;}
    public Material initial(){return env.getProperty("app.signing-keystore","").isBlank()?prepare("GENERATE"):prepare(env.getProperty("app.signing-alias","idp"));}
    public Material prepare(String alias){
        if(!alias.matches("[A-Za-z0-9_-]{1,64}"))throw new IllegalArgumentException("Invalid signing alias");
        try{
            if(!env.getProperty("app.signing-keystore","").isBlank())return new Material(fromKeystore(alias),"alias:"+alias);
            if(!env.matchesProfiles("local","test"))throw new IllegalStateException("Production requires persistent signing keystore");
            if(!alias.equals("GENERATE"))throw new IllegalArgumentException("Use GENERATE for local key material");
            var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(3072);var pair=generator.generateKeyPair();
            String kid=fingerprint((RSAPublicKey)pair.getPublic());
            var key=new RSAKey.Builder((RSAPublicKey)pair.getPublic()).privateKey((RSAPrivateKey)pair.getPrivate()).keyID(kid).build();
            if(env.matchesProfiles("test")){memory.put(kid,key);return new Material(key,"memory:"+kid);}
            Path path=localPath(kid);Files.createDirectories(path.getParent());
            byte[] encoded=pair.getPrivate().getEncoded();
            try{Files.writeString(path,secrets.encrypt(encoded,"signing:"+kid),StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);}finally{Arrays.fill(encoded,(byte)0);}
            return new Material(key,"local:"+kid);
        }catch(Exception e){throw new IllegalStateException("Signing key material unavailable",e);}
    }
    public RSAKey load(String reference,RSAKey publicKey){
        try{
            RSAKey key;
            if(reference.startsWith("alias:"))key=fromKeystore(reference.substring(6));
            else if(reference.startsWith("memory:")&&env.matchesProfiles("test"))key=memory.get(reference.substring(7));
            else if(reference.startsWith("local:")&&env.matchesProfiles("local")){
                String kid=reference.substring(6);byte[] encoded=secrets.decrypt(Files.readString(localPath(kid)),"signing:"+kid);
                try{var privateKey=(RSAPrivateKey)KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));key=new RSAKey.Builder(publicKey.toRSAPublicKey()).privateKey(privateKey).keyID(kid).build();}
                finally{Arrays.fill(encoded,(byte)0);}
            }else throw new IllegalStateException("Unknown key reference");
            if(key==null||!key.getKeyID().equals(publicKey.getKeyID())||!key.toPublicJWK().computeThumbprint().equals(publicKey.computeThumbprint()))throw new IllegalStateException("Signing key fingerprint mismatch");
            return key;
        }catch(Exception e){throw new IllegalStateException("Active signing key unavailable",e);}
    }
    private Path localPath(String kid){if(!kid.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Invalid key identifier");return Path.of(".local","signing-keys",kid+".sealed");}
    private RSAKey fromKeystore(String alias)throws Exception {
        char[] password=env.getRequiredProperty("app.signing-password").toCharArray();
        try{
            var store=KeyStore.getInstance("PKCS12");try(var input=Files.newInputStream(Path.of(env.getRequiredProperty("app.signing-keystore")))){store.load(input,password);}
            if(!store.isKeyEntry(alias))throw new IllegalArgumentException("Configured key alias not found");
            var publicKey=(RSAPublicKey)store.getCertificate(alias).getPublicKey();var privateKey=(RSAPrivateKey)store.getKey(alias,password);
            if(publicKey.getModulus().bitLength()<3072||!publicKey.getPublicExponent().equals(java.math.BigInteger.valueOf(65537)))throw new IllegalArgumentException("Signing key must be RSA 3072 or stronger");
            return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(fingerprint(publicKey)).build();
        }finally{Arrays.fill(password,'\0');}
    }
    private static String fingerprint(RSAPublicKey key)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getEncoded()));}
}
