package vn.kma.idp.account.service;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;
import vn.kma.idp.shared.*;
import vn.kma.idp.account.repository.AccountRepository;
import vn.kma.idp.auth.service.AuthService;
@Service
public class AccountService {
    private final AccountRepository repository;
    private final AuthService auth;
    private final boolean simulator;
    private final vn.kma.idp.security.repository.AuthorizationRepository authorization;
    public AccountService(AccountRepository repository,AuthService auth,Environment env,vn.kma.idp.security.repository.AuthorizationRepository authorization) {
        this.repository=repository;
        this.auth=auth;this.authorization=authorization;
        simulator=env.getProperty("app.allow-simulator",Boolean.class,false);
    }
    @Transactional public Map<String,Object> onboard(String actor,String username,String name,String email) {
        String p=repository.insert("person",Map.of("display_name",name,"email",email,"status","ACTIVE","created_at",Database.now()));
        String i=repository.insert("identity",Map.of("person_id",p,"subject_id",Database.id(),"identifier",username,"identity_type","MEMBER","status","ACTIVE","created_at",Database.now()));
        String a=repository.insert("account",Map.of("identity_id",i,"username",username,"status","PENDING_ACTIVATION","is_locked",false,"failed_login_attempts",0,"created_at",Database.now()));
        repository.audit(actor,"account.create","account",a,"SUCCESS");
        return Map.of("accountId",a,"personId",p,"identityId",i);
    }
    @Transactional public Map<String,Object> enroll(String actor,String accountId,String cardId,String publicKey,String transport) {
        repository.lockAccount(accountId);
        var a=repository.findAccountById(accountId);
        if(!Set.of("PENDING_ACTIVATION","ACTIVE").contains(a.get("status"))) throw new ApiException(409,"ACCOUNT_NOT_ENROLLABLE");
        if(!Set.of("SIMULATOR","PCSC").contains(transport) || (!simulator && !"PCSC".equals(transport))) throw new ApiException(400,"INVALID_TRANSPORT");
        try {
            var key=(RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
            if(!Set.of(2048,3072,4096).contains(key.getModulus().bitLength()) || !key.getPublicExponent().equals(java.math.BigInteger.valueOf(65537))) throw new IllegalArgumentException();
        }
        catch(GeneralSecurityException|IllegalArgumentException e) {
            throw new ApiException(400,"INVALID_RSA_PUBLIC_KEY");
        }
        String credential=repository.insert("credential",Map.of("account_id",accountId,"credential_type","SMART_CARD","credential_name","Java Card "+cardId.substring(0,8),"credential_identifier",cardId,"status","ACTIVE","registered_at",Database.now()));
        repository.insert("smart_card_credential",Map.of("credential_id",credential,"card_identifier",cardId,"public_key_spki",publicKey,"algorithm","SHA256withRSA","transport",transport,"status","ACTIVE","created_at",Database.now()));
        repository.activateAccount(Database.now(),accountId);
        repository.audit(actor,"credential.smartcard.enroll","credential",credential,"SUCCESS");
        return Map.of("credentialId",credential,"status","ACTIVE");
    }
    public void protectLastAdmin(String accountId) {
        var admins=repository.findActiveSuperusers();
        if(admins.size()==1 && admins.contains(accountId)) throw new ApiException(409,"LAST_SUPERUSER_PROTECTED");
    }
    @Transactional public void lock(String actor,String id,boolean lock) {
        repository.lockSuperuserRole();
        repository.lockAccount(id);
        repository.requireAccount(id);
        if(lock) protectLastAdmin(id);
        repository.setAccountLock(lock,lock?"ADMIN_LOCK":null,Database.now(),id);
        if(lock) revokeAll(id);
        repository.audit(actor,lock?"account.lock":"account.unlock","account",id,"SUCCESS");
    }
    @Transactional public void revokeAll(String account) {
        repository.findActiveSessionIds(account).forEach(auth::revokeSession);
    }
    @Transactional public void forceLogout(String actor,String account){repository.lockAccount(account);revokeAll(account);repository.audit(actor,"account.force_logout","account",account,"SUCCESS");}
    @Transactional public void revokeCredential(String actor,String id) {
        var c=repository.findCredential(id);
        String account=c.get("account_id").toString();
        if(authorization.has(account,"idp.manage",null,null)&&!authorization.has(actor,"idp.manage",null,null))throw new ApiException(403,"PRIVILEGED_ACCOUNT_PROTECTED");
        repository.lockSuperuserRole();repository.lockAccount(account);
        if(repository.otherPrimary(account,id)==0)protectLastAdmin(account);
        repository.revokeCredential(Database.now(),id);
        repository.revokeCard(id);
        revokeAll(c.get("account_id").toString());
        repository.audit(actor,"credential.revoke","credential",id,"SUCCESS");
    }
}
