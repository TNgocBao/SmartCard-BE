package vn.kma.idp.security;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Versioned AES-256-GCM envelope. AAD prevents moving ciphertext between records/purposes. */
@Component
public class SecretBox {
    private final byte[] key;
    private final SecureRandom random=new SecureRandom();
    public SecretBox(Environment env) throws Exception {
        String configured=env.getProperty("app.crypto.master-key","");
        if(!configured.isBlank()) key=Base64.getDecoder().decode(configured);
        else if(env.matchesProfiles("test")) { key=new byte[32];random.nextBytes(key); }
        else if(env.matchesProfiles("local")) {
            Path path=Path.of(env.getProperty("app.crypto.local-key-file",".local/encryption.key"));
            Files.createDirectories(path.toAbsolutePath().getParent());
            if(!Files.exists(path)) {
                byte[] generated=new byte[32];random.nextBytes(generated);
                try { Files.write(path,generated,StandardOpenOption.CREATE_NEW); }
                catch(FileAlreadyExistsException ignored) { /* another process initialized it */ }
                finally { Arrays.fill(generated,(byte)0); }
            }
            key=Files.readAllBytes(path);
        } else throw new IllegalStateException("Production requires APP_ENCRYPTION_KEY_B64");
        if(key.length!=32) throw new IllegalStateException("Encryption key must be exactly 256 bits");
    }
    public String encrypt(byte[] clear,String purpose) {
        try {
            byte[] iv=new byte[12];random.nextBytes(iv);var cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            cipher.updateAAD(purpose.getBytes(StandardCharsets.UTF_8));
            return "v1."+Base64.getEncoder().encodeToString(iv)+"."+Base64.getEncoder().encodeToString(cipher.doFinal(clear));
        } catch(GeneralSecurityException e) { throw new IllegalStateException("Secret encryption failed",e); }
    }
    public byte[] decrypt(String encoded,String purpose) {
        try {
            String[] parts=encoded.split("\\.",-1);if(parts.length!=3 || !parts[0].equals("v1")) throw new GeneralSecurityException("Unsupported envelope");
            var cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,Base64.getDecoder().decode(parts[1])));
            cipher.updateAAD(purpose.getBytes(StandardCharsets.UTF_8));return cipher.doFinal(Base64.getDecoder().decode(parts[2]));
        } catch(GeneralSecurityException|IllegalArgumentException e) { throw new IllegalStateException("Secret authentication failed",e); }
    }
}
