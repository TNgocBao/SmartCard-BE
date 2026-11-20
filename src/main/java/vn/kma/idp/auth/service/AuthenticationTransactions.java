package vn.kma.idp.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import vn.kma.idp.auth.repository.AuthenticationTransactionStore;
import vn.kma.idp.security.SecretBox;
import vn.kma.idp.shared.*;

@Service
public class AuthenticationTransactions {
    private final AuthenticationTransactionStore repository;private final SecretBox box;private final ObjectMapper json;
    private final SecureRandom random=new SecureRandom();
    private final org.springframework.transaction.support.TransactionTemplate claims;
    public AuthenticationTransactions(AuthenticationTransactionStore repository,SecretBox box,ObjectMapper json,org.springframework.transaction.PlatformTransactionManager manager){
        this.repository=repository;this.box=box;this.json=json;
        claims=new org.springframework.transaction.support.TransactionTemplate(manager);
        claims.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    public String create(String account,String purpose,Map<String,Object> context,int ttlSeconds) {
        byte[] nonce=new byte[32];random.nextBytes(nonce);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(nonce),id=Database.id();
        repository.create(Map.of("id",id,"account_id",account,"required_acr",purpose,"transaction_token_hash",AuthService.hash(token),"oidc_context_json",encode(id,context),"expires_at",Time.format(Instant.now().plusSeconds(ttlSeconds)),"created_at",Database.now(),"failed_attempts",0));return token;
    }
    public record Transaction(String id,String account,String purpose,Map<String,Object> context) {}
    public Transaction load(String token,String... allowedPurposes) {
        Map<String,Object> row;
        try { row=repository.find(AuthService.hash(token)); } catch(ApiException e) { throw new ApiException(410,"TRANSACTION_EXPIRED"); }
        if(row.get("consumed_at")!=null || Instant.parse(row.get("expires_at").toString()).isBefore(Instant.now()) || ((Number)row.get("failed_attempts")).intValue()>=5 || !Set.of(allowedPurposes).contains(row.get("required_acr"))) throw new ApiException(410,"TRANSACTION_EXPIRED");
        String id=row.get("id").toString();
        try { return new Transaction(id,row.get("account_id").toString(),row.get("required_acr").toString(),json.readValue(box.decrypt(row.get("oidc_context_json").toString(),"transaction:"+id),new TypeReference<>(){})); }
        catch(Exception e){throw new IllegalStateException("Authentication transaction integrity failed",e);}
    }
    public void update(Transaction tx){repository.context(tx.id(),encode(tx.id(),tx.context()));}
    public void consume(Transaction tx){if(repository.consume(tx.id())!=1)throw new ApiException(410,"TRANSACTION_EXPIRED");}
    /** A WebAuthn ceremony is claimed once, even when signature validation or the subsequent write fails. */
    public Transaction claim(String token,String purpose,String account,String sid){
        return claims.execute(status->{
            var tx=load(token,purpose);
            if(account!=null&&(!account.equals(tx.account())||!Objects.equals(sid,tx.context().get("sid"))))throw ApiException.denied();
            consume(tx);return tx;
        });
    }
    public void failed(Transaction tx){repository.failed(tx.id());}
    private String encode(String id,Map<String,Object> data){try{return box.encrypt(json.writeValueAsBytes(data),"transaction:"+id);}catch(Exception e){throw new IllegalStateException(e);}}
}
