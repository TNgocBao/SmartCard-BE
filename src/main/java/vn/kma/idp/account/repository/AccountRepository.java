package vn.kma.idp.account.repository;
import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;
/** Aggregate queries kept here; standard entity CRUD is available through the JPA repositories. */
@Repository
public class AccountRepository {
    public void lockSuperuserRole() {
        db.jdbc.queryForList("SELECT id FROM role WHERE role_code='SUPER_ADMIN' FOR UPDATE");
    }
    public int otherPrimary(String account,String excluded){return db.jdbc.queryForObject("SELECT COUNT(*) FROM credential WHERE account_id=? AND id<>? AND credential_type IN ('PASSWORD','SMART_CARD','WEBAUTHN') AND status='ACTIVE' AND (expires_at IS NULL OR expires_at>?)",Integer.class,account,excluded,Database.now());}
    public void lockAccount(String id) {
        db.one("SELECT id FROM account WHERE id=? FOR UPDATE",id);
    }
    private final Database db;
    public AccountRepository(Database db) {
        this.db=db;
    }
    public String insert(String table,Map<String,?> values) {
        return db.insert(table,values);
    }
    public void audit(String actor,String action,String resource,String id,String result) {
        db.audit(actor,action,resource,id,result);
    }
    public Map<String,Object> findAccountById(Object... args) {
        return db.one("SELECT * FROM account WHERE id=?",args);
    }
    public int activateAccount(Object... args) {
        return db.jdbc.update("UPDATE account SET status='ACTIVE',updated_at=? WHERE id=?",args);
    }
    public List<String> findActiveSuperusers(Object... args) {
        return db.jdbc.queryForList("SELECT DISTINCT a.id FROM account a JOIN account_role_assignment ar ON a.id=ar.account_id JOIN role r ON ar.role_id=r.id WHERE r.role_code='SUPER_ADMIN' AND r.is_active=true AND ar.status='ACTIVE' AND ar.organization_id IS NULL AND ar.application_id IS NULL AND (ar.valid_from IS NULL OR ar.valid_from<=?) AND (ar.valid_until IS NULL OR ar.valid_until>?) AND a.status='ACTIVE' AND a.is_locked=false AND (a.locked_until IS NULL OR a.locked_until<=?)",String.class,Database.now(),Database.now(),Database.now());
    }
    public Map<String,Object> requireAccount(Object... args) {
        return db.one("SELECT id FROM account WHERE id=?",args);
    }
    public int setAccountLock(Object... args) {
        return db.jdbc.update("UPDATE account SET is_locked=?,failed_login_attempts=0,locked_until=NULL,lock_reason=?,updated_at=? WHERE id=?",args);
    }
    public List<String> findActiveSessionIds(Object... args) {
        return db.jdbc.queryForList("SELECT id FROM idp_session WHERE account_id=? AND status='ACTIVE'",String.class,args);
    }
    public Map<String,Object> findCredential(Object... args) {
        return db.one("SELECT * FROM credential WHERE id=?",args);
    }
    public int revokeCredential(Object... args) {
        return db.jdbc.update("UPDATE credential SET status='REVOKED',revoked_at=? WHERE id=?",args);
    }
    public int revokeCard(Object... args) {
        return db.jdbc.update("UPDATE smart_card_credential SET status='REVOKED' WHERE credential_id=?",args);
    }
}
