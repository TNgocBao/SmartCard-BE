package vn.kma.idp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.webauthn4j.converter.util.ObjectConverter;

/** Test fixture only: constructs and signs actual WebAuthn wire data without claiming hardware or biometric proof. */
public final class VirtualWebAuthnAuthenticator {
    private final KeyPair key;
    private final byte[] id=new byte[32];
    private final ObjectMapper json=new ObjectMapper();
    public VirtualWebAuthnAuthenticator()throws Exception {var generator=KeyPairGenerator.getInstance("EC");generator.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));key=generator.generateKeyPair();new SecureRandom().nextBytes(id);}
    public String id(){return encode(id);}
    private static String encode(byte[] bytes){return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
    private byte[] client(JsonNode options,String type,String origin)throws Exception {return json.writeValueAsBytes(Map.of("type",type,"challenge",options.path("publicKey").path("challenge").asText(),"origin",origin,"crossOrigin",false));}
    private byte[] coordinate(java.math.BigInteger n){byte[] raw=n.toByteArray(),out=new byte[32];int length=Math.min(32,raw.length);System.arraycopy(raw,raw.length-length,out,32-length,length);return out;}
    public Map<String,Object> registration(JsonNode options,String origin,int flags)throws Exception {
        var publicKey=(ECPublicKey)key.getPublic();
        var cbor=new ObjectConverter().getCborConverter();
        byte[] cose=cbor.writeValueAsBytes(Map.of(1,2,3,-7,-1,1,-2,coordinate(publicKey.getW().getAffineX()),-3,coordinate(publicKey.getW().getAffineY())));
        byte[] rp=MessageDigest.getInstance("SHA-256").digest("localhost".getBytes(StandardCharsets.UTF_8));
        byte[] auth=ByteBuffer.allocate(32+1+4+16+2+id.length+cose.length).put(rp).put((byte)flags).putInt(0).put(new byte[16]).putShort((short)id.length).put(id).put(cose).array();
        byte[] attestation=cbor.writeValueAsBytes(Map.of("fmt","none","authData",auth,"attStmt",Map.of()));
        return Map.of("transactionToken",options.path("transactionToken").asText(),"attestationObject",encode(attestation),"clientDataJSON",encode(client(options,"webauthn.create",origin)));
    }
    public Map<String,Object> assertion(JsonNode options,String origin,String rp,int flags,int counter)throws Exception {
        byte[] auth=ByteBuffer.allocate(37).put(MessageDigest.getInstance("SHA-256").digest(rp.getBytes(StandardCharsets.UTF_8))).put((byte)flags).putInt(counter).array();
        byte[] client=client(options,"webauthn.get",origin);
        var signature=Signature.getInstance("SHA256withECDSA");signature.initSign(key.getPrivate());signature.update(auth);signature.update(MessageDigest.getInstance("SHA-256").digest(client));
        return new LinkedHashMap<>(Map.of("transactionToken",options.path("transactionToken").asText(),"credentialId",id(),"authenticatorData",encode(auth),"clientDataJSON",encode(client),"signature",encode(signature.sign())));
    }
}
