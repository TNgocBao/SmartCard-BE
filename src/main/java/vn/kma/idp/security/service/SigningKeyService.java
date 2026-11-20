package vn.kma.idp.security.service;

import java.time.Instant;
import java.util.*;
import com.nimbusds.jose.jwk.*;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import vn.kma.idp.security.SigningKeyMaterial;
import vn.kma.idp.security.repository.SigningKeyStore;
import vn.kma.idp.shared.*;

@Service
@DependsOnDatabaseInitialization
public class SigningKeyService {
    private final SigningKeyStore repository;private final SigningKeyMaterial material;
    private final Map<String,RSAKey> privateKeys=new java.util.concurrent.ConcurrentHashMap<>();
    public SigningKeyService(SigningKeyStore repository,SigningKeyMaterial material,org.springframework.transaction.PlatformTransactionManager manager){
        this.repository=repository;this.material=material;
        new TransactionTemplate(manager).execute(status->{
            if(repository.count()==0){var first=material.initial();insert(first,"ACTIVE");}
            if(repository.active().size()!=1)throw new IllegalStateException("Exactly one active signing key is required");
            active();return null;
        });
    }
    private String insert(SigningKeyMaterial.Material key,String status){
        var values=new LinkedHashMap<String,Object>();values.put("kid",key.key().getKeyID());values.put("algorithm","RS256");values.put("public_jwk",key.key().toPublicJWK().toJSONString());values.put("private_key_ref",key.reference());values.put("status",status);values.put("not_before",Database.now());values.put("expires_at",Time.format(Instant.now().plusSeconds(31536000)));values.put("created_at",Database.now());if(status.equals("ACTIVE"))values.put("active_slot",1);
        String id=repository.insert(values);privateKeys.put(key.key().getKeyID(),key.key());return id;
    }
    public RSAKey active(){
        var rows=repository.active();if(rows.size()!=1)throw new IllegalStateException("Active signing key missing");var row=rows.getFirst();
        if(!Instant.parse(row.get("expires_at").toString()).isAfter(Instant.now().plusSeconds(300)))throw new IllegalStateException("Active signing key needs rotation");
        RSAKey publicKey=publicKey(row);
        return privateKeys.computeIfAbsent(publicKey.getKeyID(),kid->material.load(row.get("private_key_ref").toString(),publicKey));
    }
    public RSAKey verification(String kid){
        Map<String,Object> row;try{row=repository.byKid(kid);}catch(ApiException e){throw new ApiException(401,"INVALID_TOKEN");}
        if(!Set.of("ACTIVE","RETIRED").contains(row.get("status"))||!Instant.parse(row.get("expires_at").toString()).isAfter(Instant.now())||Instant.parse(row.get("not_before").toString()).isAfter(Instant.now()))throw new ApiException(401,"INVALID_TOKEN");
        return publicKey(row);
    }
    private RSAKey publicKey(Map<String,Object> row){try{return RSAKey.parse(row.get("public_jwk").toString()).toPublicJWK();}catch(Exception e){throw new IllegalStateException("Invalid persisted public signing key",e);}}
    public Map<String,Object> jwks(){return new JWKSet(repository.publicKeys().stream().map(this::publicKey).map(k->(JWK)k).toList()).toJSONObject();}
    public Object list(){return Map.of("data",repository.metadata());}
    @Transactional public Map<String,Object> prepare(String actor,String alias){
        repository.lockActive();if(repository.pending()>=3)throw new ApiException(409,"TOO_MANY_NEXT_KEYS");
        String id=insert(material.prepare(alias),"NEXT");repository.audit(actor,"signing_key.prepared",id);return Map.of("id",id,"status","NEXT");
    }
    @Transactional public Map<String,Object> rotate(String actor,String id,int overlapSeconds){
        if(overlapSeconds<300||overlapSeconds>28800)throw new ApiException(400,"INVALID_KEY_OVERLAP");
        var old=repository.lockActive();var next=repository.require(id);
        if(!"NEXT".equals(next.get("status")))throw new ApiException(409,"KEY_NOT_NEXT");
        if(!Instant.parse(next.get("expires_at").toString()).isAfter(Instant.now().plusSeconds(300)))throw new ApiException(409,"KEY_EXPIRED");
        RSAKey key=material.load(next.get("private_key_ref").toString(),publicKey(next));privateKeys.put(key.getKeyID(),key);
        repository.retire(old.get("id").toString(),Time.format(Instant.now().plusSeconds(overlapSeconds)));repository.activate(id);repository.audit(actor,"signing_key.rotated",id);return Map.of("id",id,"kid",key.getKeyID(),"status","ACTIVE","overlapSeconds",overlapSeconds);
    }
    @Transactional public void revoke(String actor,String id){
        repository.lockActive();var row=repository.require(id);if("ACTIVE".equals(row.get("status")))throw new ApiException(409,"ROTATE_ACTIVE_KEY_FIRST");
        boolean retired="RETIRED".equals(row.get("status"));repository.revoke(id);privateKeys.remove(row.get("kid").toString());if(retired)repository.revokeSessions();repository.audit(actor,"signing_key.revoked",id);
    }
}
