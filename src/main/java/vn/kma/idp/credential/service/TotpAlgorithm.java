package vn.kma.idp.credential.service;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** RFC 6238 / RFC 4226, SHA-1, configurable digits for reference-vector tests. */
public final class TotpAlgorithm {
    private static final String ALPHABET="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private TotpAlgorithm() {}
    public static String code(byte[] secret,long step,int digits) {
        try {
            Mac mac=Mac.getInstance("HmacSHA1");mac.init(new SecretKeySpec(secret,"HmacSHA1"));
            byte[] hash=mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());int offset=hash[hash.length-1]&15;
            int binary=((hash[offset]&127)<<24)|((hash[offset+1]&255)<<16)|((hash[offset+2]&255)<<8)|(hash[offset+3]&255);
            int divisor=digits==8?100000000:1000000;
            return String.format(java.util.Locale.ROOT,"%0"+digits+"d",binary%divisor);
        } catch(Exception e) { throw new IllegalStateException("TOTP calculation failed",e); }
    }
    public static long matchingStep(byte[] secret,String submitted,long nowEpochSeconds) {
        if(submitted==null || !submitted.matches("[0-9]{6}")) return -1;
        long step=nowEpochSeconds/30;
        for(long candidate=step-1;candidate<=step+1;candidate++) if(candidate>=0 && MessageDigest.isEqual(code(secret,candidate,6).getBytes(StandardCharsets.US_ASCII),submitted.getBytes(StandardCharsets.US_ASCII))) return candidate;
        return -1;
    }
    public static String base32(byte[] data) {
        var output=new StringBuilder();int buffer=0,bits=0;
        for(byte value:data) { buffer=(buffer<<8)|(value&255);bits+=8;while(bits>=5){bits-=5;output.append(ALPHABET.charAt((buffer>>bits)&31));} }
        if(bits>0) output.append(ALPHABET.charAt((buffer<<(5-bits))&31));return output.toString();
    }
}
