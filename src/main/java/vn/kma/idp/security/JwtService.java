package vn.kma.idp.security;
import java.time.Instant;
import java.util.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.stereotype.Service;
import vn.kma.idp.shared.ApiException;
import com.fasterxml.jackson.core.*;
/** Fixed RS256 allowlist; signature and registered claims are validated before using any claim. */
@Service
public class JwtService {
    private static final JsonFactory JWT_JSON=JsonFactory.builder()
        .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(16).maxStringLength(4096).maxNumberLength(64).build())
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
    private final Keys keys;
    public JwtService(Keys keys){
        this.keys=keys;
    }
    public String issue(String accountId,String sid,List<String> permissions,String method) {
        try {
            Instant now=Instant.now();
            var claims=new JWTClaimsSet.Builder().issuer(keys.issuer).subject(accountId).audience("kma-idp-api")
            .issueTime(Date.from(now)).notBeforeTime(Date.from(now.minusSeconds(1))).expirationTime(Date.from(now.plusSeconds(300)))
            .jwtID(UUID.randomUUID().toString()).claim("sid",sid).claim("permissions",permissions).claim("amr",Arrays.asList(method.split("\\+"))).claim("acr",method.contains("+TOTP")||method.equals("WEBAUTHN_UV")?"AAL2":"AAL1").build();
            var key=keys.active();
            var token=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).type(JOSEObjectType.JWT).build(),claims);
            token.sign(new RSASSASigner(key));
            return token.serialize();
        }
        catch(JOSEException e){
            throw new IllegalStateException("Token signing failed",e);
        }
    }
    public UserPrincipal verify(String raw) {
        try {
            validateStructure(raw);
            var token=SignedJWT.parse(raw);
            if(!JWSAlgorithm.RS256.equals(token.getHeader().getAlgorithm()) || token.getHeader().getKeyID()==null || !token.verify(new RSASSAVerifier(keys.verification(token.getHeader().getKeyID()).toRSAPublicKey()))) throw new ApiException(401,"INVALID_TOKEN");
            var c=token.getJWTClaimsSet();
            Date now=new Date();
            if(!keys.issuer.equals(c.getIssuer()) || !c.getAudience().contains("kma-idp-api") || c.getExpirationTime()==null || !c.getExpirationTime().after(now) || c.getNotBeforeTime()==null || c.getNotBeforeTime().after(now) || c.getSubject()==null || c.getStringClaim("sid")==null) throw new ApiException(401,"INVALID_TOKEN");
            return new UserPrincipal(c.getSubject(),c.getStringClaim("sid"),List.copyOf(c.getStringListClaim("permissions")));
        }
        catch(org.springframework.dao.DataAccessException e){throw e;}
        catch(Exception e){
            throw new ApiException(401,"INVALID_TOKEN");
        }
    }
    private void validateStructure(String raw) throws Exception {
        if(raw==null || raw.length()>8192) throw new ApiException(401,"INVALID_TOKEN");
        String[] parts=raw.split("\\.",-1);
        if(parts.length!=3) throw new ApiException(401,"INVALID_TOKEN");
        for(String part:parts) if(!part.matches("[A-Za-z0-9_-]+")) throw new ApiException(401,"INVALID_TOKEN");
        for(int i=0;i<2;i++) {
            byte[] decoded=Base64.getUrlDecoder().decode(parts[i]);
            try(var parser=JWT_JSON.createParser(decoded)) {
                if(parser.nextToken()!=JsonToken.START_OBJECT) throw new ApiException(401,"INVALID_TOKEN");
                int depth=1;
                while(depth>0) {
                    JsonToken token=parser.nextToken();
                    if(token==null) throw new ApiException(401,"INVALID_TOKEN");
                    if(token==JsonToken.START_OBJECT || token==JsonToken.START_ARRAY) depth++;
                    if(token==JsonToken.END_OBJECT || token==JsonToken.END_ARRAY) depth--;
                }
                if(parser.nextToken()!=null) throw new ApiException(401,"INVALID_TOKEN");
            }
        }
    }
}
