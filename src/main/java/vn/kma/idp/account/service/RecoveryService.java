package vn.kma.idp.account.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;
import vn.kma.idp.account.repository.RecoveryStore;
import vn.kma.idp.auth.service.*;
import vn.kma.idp.audit.service.OutboxService;
import vn.kma.idp.shared.*;

@Service
public class RecoveryService {
    private final RecoveryStore repository;private final AuthenticationTransactions transactions;private final OutboxService outbox;private final AccountService accounts;private final boolean lab;
    private final SecureRandom random=new SecureRandom();
    public RecoveryService(RecoveryStore repository,AuthenticationTransactions transactions,OutboxService outbox,AccountService accounts,Environment env){this.repository=repository;this.transactions=transactions;this.outbox=outbox;this.accounts=accounts;lab=env.matchesProfiles("local","test");}
    private String opaque(){byte[] bytes=new byte[32];random.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
    @Transactional public Map<String,Object> invite(String actor,String account) {
        var a=repository.account(account);
        if(!Set.of("PENDING_ACTIVATION","ACTIVE").contains(a.get("status")) || Boolean.TRUE.equals(a.get("is_locked")))throw new ApiException(409,"ACCOUNT_NOT_ENROLLABLE");
        String email=Objects.toString(a.get("email"),"");if(email.isBlank())throw new ApiException(409,"VERIFIED_CONTACT_REQUIRED");
        String raw=opaque();repository.token(Map.of("account_id",account,"token_hash",AuthService.hash(raw),"purpose","ACTIVATION","expires_at",Time.format(Instant.now().plusSeconds(1800)),"created_at",Database.now()));
        String delivery=outbox.email(email,"KMA IdP — activation","Use this one-time activation token within 30 minutes: "+raw);
        repository.audit(actor,"account.activation.invited",account);
        var result=new HashMap<String,Object>();result.put("status","QUEUED");result.put("deliveryId",delivery);if(lab)result.put("oneTimeActivationToken",raw);return result;
    }
    @Transactional public Map<String,Object> activate(String token) {
        Map<String,Object> row;
        try{row=repository.activation(AuthService.hash(token));}catch(ApiException e){throw new ApiException(410,"CHALLENGE_EXPIRED");}
        if(!"ACTIVATION".equals(row.get("purpose")) || repository.consumeActivation(row.get("id").toString())!=1)throw new ApiException(410,"CHALLENGE_EXPIRED");
        var account=repository.account(row.get("account_id").toString());
        if(Boolean.TRUE.equals(account.get("is_locked")) || !Set.of("ACTIVE","PENDING_ACTIVATION").contains(account.get("status")))throw new ApiException(423,"ACCOUNT_LOCKED");
        return Map.of("enrollmentToken",transactions.create(account.get("id").toString(),"ENROLLMENT",new HashMap<>(Map.of("primary","ACTIVATION")),600),"expiresIn",600);
    }
    @Transactional public List<String> regenerate(String account) {
        repository.revokeRecovery(account);var codes=new ArrayList<String>();
        for(int i=0;i<10;i++){String code=opaque();repository.recoveryCode(account,AuthService.hash(account+":"+code));codes.add(code);}
        repository.audit(account,"recovery.codes.regenerated",account);return codes;
    }
    @Transactional public Map<String,Object> recover(String username,String code) {
        Map<String,Object> account;
        try{account=repository.byUsername(username);}catch(ApiException e){return Map.of("error","AUTHENTICATION_FAILED");}
        String id=account.get("id").toString();
        if(!"ACTIVE".equals(account.get("status")) || Boolean.TRUE.equals(account.get("is_locked")))return Map.of("error","AUTHENTICATION_FAILED");
        if(repository.consumeRecovery(id,AuthService.hash(id+":"+code))!=1)return Map.of("error","AUTHENTICATION_FAILED");
        repository.reset(id);repository.audit(id,"account.recovery.started",id);
        return Map.of("enrollmentToken",transactions.create(id,"RECOVERY_ENROLLMENT",new HashMap<>(Map.of("primary","RECOVERY_CODE")),600),"expiresIn",600);
    }
    @Transactional public Map<String,Object> resetMfa(String actor,String account,String reason) {
        if(reason==null || reason.trim().length()<5)throw new ApiException(400,"RESET_REASON_REQUIRED");
        accounts.protectLastAdmin(account);repository.reset(account);repository.audit(actor,"account.mfa.reset",account);return invite(actor,account);
    }
}
