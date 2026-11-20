package vn.kma.idp.account.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;

@Repository
public class RecoveryStore {
    private final Database db;
    public RecoveryStore(Database db){this.db=db;}
    public Map<String,Object> account(String id){return db.one("SELECT a.*,p.email FROM account a JOIN identity i ON a.identity_id=i.id JOIN person p ON p.id=i.person_id WHERE a.id=? FOR UPDATE",id);}
    public Map<String,Object> byUsername(String username){return db.one("SELECT * FROM account WHERE username=?",username);}
    public String token(Map<String,Object> values){return db.insert("account_activation_token",values);}
    public Map<String,Object> activation(String hash){return db.one("SELECT * FROM account_activation_token WHERE token_hash=? FOR UPDATE",hash);}
    public int consumeActivation(String id){return db.jdbc.update("UPDATE account_activation_token SET consumed_at=? WHERE id=? AND consumed_at IS NULL AND expires_at>?",Database.now(),id,Database.now());}
    public void revokeRecovery(String account){db.jdbc.update("UPDATE recovery_code SET revoked_at=? WHERE account_id=? AND revoked_at IS NULL",Database.now(),account);}
    public void recoveryCode(String account,String hash){db.insert("recovery_code",Map.of("account_id",account,"code_hash",hash,"created_at",Database.now()));}
    public int consumeRecovery(String account,String hash){return db.jdbc.update("UPDATE recovery_code SET used_at=? WHERE account_id=? AND code_hash=? AND used_at IS NULL AND revoked_at IS NULL",Database.now(),account,hash);}
    public void reset(String account){
        db.jdbc.update("UPDATE credential SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
        db.jdbc.update("UPDATE smart_card_credential SET status='REVOKED' WHERE credential_id IN (SELECT id FROM credential WHERE account_id=?)",account);
        db.jdbc.update("UPDATE idp_session SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
        db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
        db.jdbc.update("UPDATE account SET status='PENDING_ACTIVATION',updated_at=? WHERE id=?",Database.now(),account);
        db.jdbc.update("UPDATE account_activation_token SET consumed_at=? WHERE account_id=? AND consumed_at IS NULL",Database.now(),account);
        revokeRecovery(account);
    }
    public void audit(String actor,String action,String account){db.audit(actor,action,"account",account,"SUCCESS");}
}
