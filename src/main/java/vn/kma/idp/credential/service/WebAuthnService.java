package vn.kma.idp.credential.service;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.auth.service.*;
import vn.kma.idp.credential.dto.*;
import vn.kma.idp.credential.repository.WebAuthnStore;
import vn.kma.idp.security.UserPrincipal;
import vn.kma.idp.shared.*;

@Service
public class WebAuthnService {
    private final WebAuthnStore repository;
    private final WebAuthnVerifier verifier;
    private final AuthenticationTransactions transactions;
    private final AuthService auth;
    public WebAuthnService(WebAuthnStore repository,WebAuthnVerifier verifier,AuthenticationTransactions transactions,AuthService auth){this.repository=repository;this.verifier=verifier;this.transactions=transactions;this.auth=auth;}
    private String nonce(){byte[] bytes=new byte[32];new java.security.SecureRandom().nextBytes(bytes);return WebAuthnVerifier.encode(bytes);}
    private List<Map<String,String>> descriptors(String account){return repository.credentialIds(account).stream().map(id->Map.of("type","public-key","id",id)).toList();}
    @Transactional public Map<String,Object> registrationOptions(UserPrincipal user){
        var account=repository.account(user.accountId());auth.assertActive(account);
        String challenge=nonce(),handle=repository.handle(user.accountId());
        String token=transactions.create(user.accountId(),"WEBAUTHN_REGISTER",Map.of("challenge",challenge,"handle",handle,"sid",user.sessionId()),120);
        return Map.of("transactionToken",token,"publicKey",Map.of("challenge",challenge,"rp",Map.of("id",verifier.rpId(),"name","KMA IdP"),"user",Map.of("id",handle,"name",account.get("username"),"displayName",account.get("username")),"pubKeyCredParams",List.of(Map.of("type","public-key","alg",-7)),"timeout",120000,"attestation","none","authenticatorSelection",Map.of("userVerification","required"),"excludeCredentials",descriptors(user.accountId())));
    }
    @Transactional public Map<String,Object> register(UserPrincipal user,WebAuthnRegistration request){
        var tx=transactions.claim(request.transactionToken(),"WEBAUTHN_REGISTER",user.accountId(),user.sessionId());
        auth.assertActive(repository.account(tx.account()));
        Map<String,Object> record;
        try{record=verifier.registration(tx.context().get("challenge").toString(),tx.context().get("handle").toString(),request);}
        catch(RuntimeException e){repository.failed(tx.account(),tx.id(),"registration_failed");return Map.of("error","WEBAUTHN_VERIFICATION_FAILED");}
        return Map.of("credentialId",repository.register(tx.account(),record),"status","ACTIVE");
    }
    @Transactional public Map<String,Object> authenticationOptions(String username){
        Map<String,Object> account;
        try{account=repository.byUsername(username);}catch(ApiException e){throw new ApiException(401,"AUTHENTICATION_FAILED");}
        auth.assertActive(account);String id=account.get("id").toString(),challenge=nonce();
        var allowed=descriptors(id);if(allowed.isEmpty())throw new ApiException(401,"AUTHENTICATION_FAILED");
        return Map.of("transactionToken",transactions.create(id,"WEBAUTHN_AUTHENTICATE",Map.of("challenge",challenge),120),"publicKey",Map.of("challenge",challenge,"rpId",verifier.rpId(),"allowCredentials",allowed,"userVerification","required","timeout",120000));
    }
    @Transactional public Map<String,Object> authenticate(WebAuthnAssertion proof){
        var tx=transactions.claim(proof.transactionToken(),"WEBAUTHN_AUTHENTICATE",null,null);
        auth.assertActive(repository.account(tx.account()));
        Map<String,Object> stored;WebAuthnVerifier.Verified verified;
        try{
            stored=repository.credential(tx.account(),WebAuthnVerifier.encode(WebAuthnVerifier.decode(proof.credentialId())));
            if(!"ACTIVE".equals(stored.get("credential_status"))||stored.get("expires_at")!=null&&!Instant.parse(stored.get("expires_at").toString()).isAfter(Instant.now()))throw new IllegalArgumentException();
            verified=verifier.assertion(tx.context().get("challenge").toString(),stored,proof);
        }catch(RuntimeException e){repository.failed(tx.account(),tx.id(),"assertion_failed");return Map.of("error","WEBAUTHN_VERIFICATION_FAILED");}
        repository.used(tx.account(),stored.get("credential_id").toString(),verified.counter(),verified.backedUp());
        return auth.createSession(tx.account(),"WEBAUTHN_UV");
    }
}
