package vn.kma.idp.auth.service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.kma.idp.security.JwtService;
import org.springframework.core.env.Environment;
import vn.kma.idp.security.Keys;
import vn.kma.idp.shared.*;
import vn.kma.idp.auth.repository.AuthRepository;
@Service
public class AuthService {
    private final AuthRepository repository;
    private final JwtService jwt;
    private final BCryptPasswordEncoder passwords;
    private final boolean allowSimulator;
    private final SecureRandom random=new SecureRandom();
    private final org.springframework.transaction.support.TransactionTemplate transactions;
    private final org.springframework.beans.factory.ObjectProvider<MfaService> mfa;
    public AuthService(AuthRepository repository,JwtService jwt,BCryptPasswordEncoder passwords,Environment env,org.springframework.transaction.PlatformTransactionManager manager,org.springframework.beans.factory.ObjectProvider<MfaService> mfa) {
        this.repository=repository;
        this.jwt=jwt;
        this.passwords=passwords;
        this.transactions=new org.springframework.transaction.support.TransactionTemplate(manager);
        this.mfa=mfa;
        allowSimulator=env.getProperty("app.allow-simulator",Boolean.class,false);
    }
    public static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch(GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
    private String opaque() {
        byte[] b=new byte[32];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
    private Map<String,Object> account(String username) {
        var rows=repository.findAccountByUsername(username);
        if(rows.isEmpty()) throw new ApiException(401,"AUTHENTICATION_FAILED");
        var a=rows.getFirst();
        assertActive(a);
        return a;
    }
    public void assertActive(Map<String,Object> a) {
        if(!"ACTIVE".equals(a.get("status")) || Boolean.TRUE.equals(a.get("is_locked")) ||
        (a.get("locked_until")!=null && Instant.parse(a.get("locked_until").toString()).isAfter(Instant.now())))
        throw new ApiException(423,"ACCOUNT_LOCKED");
    }
    // Password is bootstrap/admin fallback, not advertised as passwordless authentication.
    public Map<String,Object> passwordLogin(String username,String password) {
        var a=account(username);
        var credentials=repository.findPasswordCredentials(a.get("id"));
        boolean ok=!credentials.isEmpty() && passwords.matches(password,credentials.getFirst().get("secret_hash").toString());
        if(!ok) {
            repository.incrementFailedLogins(a.get("id"));
            repository.lockAfterFailedLogins(a.get("id"));
            if(Boolean.TRUE.equals(repository.findAccountById(a.get("id")).get("is_locked"))) repository.revokeAllAccountSessions(a.get("id").toString());
            repository.audit(a.get("id").toString(),"auth.password.failed","account",a.get("id").toString(),"FAILURE");
            throw new ApiException(401,"AUTHENTICATION_FAILED");
        }
        return mfa.getObject().afterPrimary(a.get("id").toString(),"PASSWORD");
    }
    @Transactional
    public Map<String,Object> challenge(String username,String cardIdentifier) {
        var a=account(username);
        var rows=repository.findCardForAccount(a.get("id"),cardIdentifier,Database.now());
        if(rows.isEmpty()) throw new ApiException(401,"AUTHENTICATION_FAILED");
        var c=rows.getFirst();
        if(!allowSimulator && !"PCSC".equals(c.get("transport"))) throw new ApiException(403,"SIMULATOR_DISABLED");
        String id=Database.id();
        // Domain separated, random and bound to account/credential in the challenge record.
        byte[] nonce=new byte[32];
        random.nextBytes(nonce);
        byte[] prefix="KMA-IDP-SMARTCARD-AUTH-V1:".getBytes(StandardCharsets.US_ASCII);
        byte[] bytes=new byte[prefix.length+nonce.length];
        System.arraycopy(prefix,0,bytes,0,prefix.length);
        System.arraycopy(nonce,0,bytes,prefix.length,nonce.length);
        String payload=Base64.getEncoder().encodeToString(bytes),expiry=Time.format(Instant.now().plusSeconds(60));
        repository.insert("smart_card_challenge",Map.of("id",id,"account_id",a.get("id"),"credential_id",c.get("id"),"challenge_hash",hash(payload),"challenge_payload",payload,"expires_at",expiry,"created_at",Database.now()));
        return Map.of("challengeId",id,"payload",payload,"expiresAt",expiry,"algorithm","SHA256withRSA");
    }
    @Transactional
    public Map<String,Object> verify(String challengeId,String signature) {
        var rows=repository.findChallengeWithCard(challengeId);
        if(rows.isEmpty()) throw new ApiException(401,"AUTHENTICATION_FAILED");
        var c=rows.getFirst();
        if(c.get("consumed_at")!=null || Instant.parse(c.get("expires_at").toString()).isBefore(Instant.now())) throw new ApiException(410,"CHALLENGE_EXPIRED");
        if(!allowSimulator && !"PCSC".equals(c.get("transport"))) throw new ApiException(403,"SIMULATOR_DISABLED");
        if(c.get("credential_expires_at")!=null && !Instant.parse(c.get("credential_expires_at").toString()).isAfter(Instant.now())) throw new ApiException(401,"CREDENTIAL_EXPIRED");
        if(!"ACTIVE".equals(c.get("card_status")) || !"ACTIVE".equals(c.get("credential_status"))) throw new ApiException(401,"CREDENTIAL_REVOKED");
        assertActive(repository.findAccountById(c.get("account_id")));
        try {
            var key=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(c.get("public_key_spki").toString())));
            var verifier=Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key);
            verifier.update(Base64.getDecoder().decode(c.get("challenge_payload").toString()));
            if(!verifier.verify(Base64.getDecoder().decode(signature))) throw new ApiException(401,"AUTHENTICATION_FAILED");
        }
        catch(GeneralSecurityException|IllegalArgumentException e) {
            throw new ApiException(401,"AUTHENTICATION_FAILED");
        }
        int changed=repository.consumeChallenge(Database.now(),challengeId,Database.now());
        if(changed!=1) throw new ApiException(410,"CHALLENGE_EXPIRED");
        repository.recordCredentialUse(Database.now(),c.get("credential_id"));
        return mfa.getObject().afterPrimary(c.get("account_id").toString(),"SMART_CARD");
    }
    @Transactional
    public Map<String,Object> createSession(String accountId,String method) {
        return transactions.execute(status -> issueSession(accountId,method));
    }
    private Map<String,Object> issueSession(String accountId,String method) {
        assertActive(repository.findAccountForUpdate(accountId));
        String refresh=opaque(),sid=Database.id();
        String level=method.contains("+TOTP")||method.equals("WEBAUTHN_UV")?"AAL2":"AAL1";
        repository.insert("idp_session",Map.of("id",sid,"account_id",accountId,"session_id_hash",hash(opaque()),"status","ACTIVE","authentication_level",level,"amr",method,"authenticated_at",Database.now(),"last_activity_at",Database.now(),"expires_at",Time.format(Instant.now().plusSeconds(28800))));
        repository.insert("refresh_token",Map.of("account_id",accountId,"session_id",sid,"token_hash",hash(refresh),"family_id",Database.id(),"status","ACTIVE","issued_at",Database.now(),"expires_at",Time.format(Instant.now().plusSeconds(28800))));
        repository.recordAccountLogin(Database.now(),accountId);
        repository.insert("authentication_event",Map.of("account_id",accountId,"authentication_method",method,"event_type","LOGIN","result","SUCCESS","created_at",Database.now()));
        repository.audit(accountId,"auth.login","idp_session",sid,"SUCCESS");
        return tokens(accountId,sid,refresh,method);
    }
    private Map<String,Object> tokens(String accountId,String sid,String refresh,String method) {
        var permissions=repository.findEffectiveGlobalPermissions(accountId,Database.now(),Database.now());
        return Map.of("accessToken",jwt.issue(accountId,sid,permissions,method),"refreshToken",refresh,"expiresIn",300,"tokenType","Bearer","sessionId",sid);
    }
    @Transactional
    public Map<String,Object> refresh(String raw) {
        var rows=repository.findRefreshToken(hash(raw));
        if(rows.isEmpty()) return Map.of("error","INVALID_REFRESH_TOKEN");
        var t=rows.getFirst();
        if(t.get("consumed_at")!=null) {
            repository.revokeTokenFamily(Database.now(),t.get("family_id"));
            revokeSession(t.get("session_id").toString());
            repository.audit(t.get("account_id").toString(),"auth.refresh.reuse","idp_session",t.get("session_id").toString(),"FAILURE");
            return Map.of("error","REFRESH_TOKEN_REUSE");
        }
        var session=repository.findSession(t.get("session_id"));
        assertActive(repository.findAccountForUpdate(t.get("account_id").toString()));
        if(!"ACTIVE".equals(t.get("status")) || !repository.isSessionActive(t.get("session_id").toString(),t.get("account_id").toString()) || !Instant.parse(t.get("expires_at").toString()).isAfter(Instant.now())) return Map.of("error","INVALID_REFRESH_TOKEN");
        if(repository.consumeRefreshToken(Database.now(),t.get("id"))!=1) {
            repository.revokeTokenFamily(Database.now(),t.get("family_id"));
            revokeSession(t.get("session_id").toString());
            repository.audit(t.get("account_id").toString(),"auth.refresh.concurrent-reuse","idp_session",t.get("session_id").toString(),"FAILURE");
            return Map.of("error","REFRESH_TOKEN_REUSE");
        }
        String next=opaque(),nextId=Database.id();
        repository.insert("refresh_token",Map.of("id",nextId,"account_id",t.get("account_id"),"session_id",t.get("session_id"),"token_hash",hash(next),"family_id",t.get("family_id"),"status","ACTIVE","issued_at",Database.now(),"expires_at",t.get("expires_at")));
        repository.linkReplacementToken(nextId,t.get("id"));
        return tokens(t.get("account_id").toString(),t.get("session_id").toString(),next,session.get("amr").toString());
    }
    public void revokeSession(String sid) {
        repository.revokeSession(Database.now(),sid);
        repository.revokeSessionTokens(Database.now(),sid);
    }
}
