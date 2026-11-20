package vn.kma.idp.credential.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;

@Repository
public class TotpStore {
    private final Database db;
    public TotpStore(Database db){this.db=db;}
    public Map<String,Object> active(String account){
        var rows=db.jdbc.queryForList("SELECT t.*,c.account_id,c.status FROM totp_credential t JOIN credential c ON c.id=t.credential_id WHERE c.account_id=? AND c.status='ACTIVE' AND t.verified_at IS NOT NULL AND (c.expires_at IS NULL OR c.expires_at>?) ORDER BY c.registered_at DESC",account,Database.now());
        return rows.isEmpty()?null:rows.getFirst();
    }
    public Map<String,Object> pending(String account,String credential){return db.one("SELECT t.*,c.account_id FROM totp_credential t JOIN credential c ON c.id=t.credential_id WHERE c.account_id=? AND c.id=? AND c.status='PENDING'",account,credential);}
    public void create(String account,String id,String encrypted){
        db.insert("credential",Map.of("id",id,"account_id",account,"credential_type","TOTP","credential_name","Authenticator","status","PENDING","registered_at",Database.now()));
        db.insert("totp_credential",Map.of("credential_id",id,"encrypted_secret",encrypted,"encryption_key_id","v1","algorithm","SHA1","digits",6,"period_seconds",30,"last_accepted_step",-1));
    }
    public int consumeStep(String credential,long step){return db.jdbc.update("UPDATE totp_credential SET last_accepted_step=? WHERE credential_id=? AND (last_accepted_step IS NULL OR last_accepted_step<?)",step,credential,step);}
    public void activate(String account,String id){
        db.jdbc.update("UPDATE idp_session SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
        db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
        db.jdbc.update("UPDATE credential SET status='REVOKED',revoked_at=? WHERE account_id=? AND credential_type='TOTP' AND id<>? AND status IN ('ACTIVE','PENDING')",Database.now(),account,id);
        db.jdbc.update("UPDATE credential SET status='ACTIVE' WHERE id=? AND account_id=?",id,account);
        db.jdbc.update("UPDATE totp_credential SET verified_at=? WHERE credential_id=?",Database.now(),id);
    }
    public void guardDisable(String account){
        db.one("SELECT id FROM account WHERE id=? FOR UPDATE",account);
        int alternatives=db.jdbc.queryForObject("SELECT COUNT(*) FROM credential WHERE account_id=? AND credential_type IN ('PASSWORD','SMART_CARD','WEBAUTHN') AND status='ACTIVE' AND (expires_at IS NULL OR expires_at>?)",Integer.class,account,Database.now());
        if(alternatives==0)throw new vn.kma.idp.shared.ApiException(409,"LAST_AUTHENTICATOR_PROTECTED");
        int privileged=db.jdbc.queryForObject("SELECT COUNT(*) FROM account_role_assignment ar JOIN role_permission rp ON ar.role_id=rp.role_id JOIN permission p ON rp.permission_id=p.id WHERE ar.account_id=? AND ar.status='ACTIVE' AND p.permission_code='idp.manage' AND p.is_active=true",Integer.class,account);
        if(privileged>0)throw new vn.kma.idp.shared.ApiException(409,"ADMIN_MFA_REQUIRED");
    }
    public void revoke(String account){db.jdbc.update("UPDATE credential SET status='REVOKED',revoked_at=? WHERE account_id=? AND credential_type='TOTP' AND status<>'REVOKED'",Database.now(),account);}
    public String username(String account){return db.one("SELECT username FROM account WHERE id=?",account).get("username").toString();}
    public void activateAccount(String account){db.jdbc.update("UPDATE account SET status='ACTIVE',updated_at=? WHERE id=? AND status='PENDING_ACTIVATION'",Database.now(),account);}
    public void audit(String account,String action,String id){db.audit(account,action,"credential",id,"SUCCESS");}
}
