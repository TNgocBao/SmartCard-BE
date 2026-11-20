package vn.kma.idp.auth.repository;
import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;
/** Aggregate queries kept here; standard entity CRUD is available through the JPA repositories. */
@Repository
public class AuthRepository {
    public Map<String,Object> findAccountForUpdate(String id) {
        return db.one("SELECT * FROM account WHERE id=? FOR UPDATE",id);
    }
    private final Database db;
    private final long idleSeconds;
    public AuthRepository(Database db,org.springframework.core.env.Environment env) {
        this.db=db;
        this.idleSeconds=env.getProperty("app.session.idle-seconds",Long.class,1800L);
        if(idleSeconds<60 || idleSeconds>28800)throw new IllegalArgumentException("Session idle timeout must be 60..28800 seconds");
    }
    public String insert(String table,Map<String,?> values) {
        return db.insert(table,values);
    }
    public void audit(String actor,String action,String resource,String id,String result) {
        db.audit(actor,action,resource,id,result);
    }
    public List<Map<String,Object>> findAccountByUsername(Object... args) {
        return db.jdbc.queryForList("SELECT * FROM account WHERE username=?",args);
    }
    public List<Map<String,Object>> findPasswordCredentials(Object... args) {
        return db.jdbc.queryForList("SELECT secret_hash FROM credential WHERE account_id=? AND credential_type='PASSWORD' AND status='ACTIVE'",args);
    }
    public int incrementFailedLogins(Object... args) {
        return db.jdbc.update("UPDATE account SET failed_login_attempts=COALESCE(failed_login_attempts,0)+1 WHERE id=?",args);
    }
    public int lockAfterFailedLogins(Object... args) {
        return db.jdbc.update("UPDATE account SET is_locked=true,lock_reason='FAILED_LOGIN_LIMIT' WHERE id=? AND failed_login_attempts>=5",args);
    }
    public List<Map<String,Object>> findCardForAccount(Object... args) {
        return db.jdbc.queryForList("SELECT c.id,s.transport FROM credential c JOIN smart_card_credential s ON c.id=s.credential_id WHERE c.account_id=? AND s.card_identifier=? AND c.status='ACTIVE' AND s.status='ACTIVE' AND (c.expires_at IS NULL OR c.expires_at>?)",args);
    }
    public List<Map<String,Object>> findChallengeWithCard(Object... args) {
        return db.jdbc.queryForList("SELECT ch.*,s.public_key_spki,s.transport,s.status AS card_status,c.status AS credential_status,c.expires_at AS credential_expires_at FROM smart_card_challenge ch JOIN smart_card_credential s ON ch.credential_id=s.credential_id JOIN credential c ON c.id=ch.credential_id WHERE ch.id=?",args);
    }
    public Map<String,Object> findAccountById(Object... args) {
        return db.one("SELECT * FROM account WHERE id=?",args);
    }
    public int consumeChallenge(Object... args) {
        return db.jdbc.update("UPDATE smart_card_challenge SET consumed_at=? WHERE id=? AND consumed_at IS NULL AND expires_at>?",args);
    }
    public int recordCredentialUse(Object... args) {
        return db.jdbc.update("UPDATE credential SET last_used_at=? WHERE id=?",args);
    }
    public int recordAccountLogin(Object... args) {
        return db.jdbc.update("UPDATE account SET failed_login_attempts=0,last_login_at=? WHERE id=?",args);
    }
    public List<String> findEffectiveGlobalPermissions(Object... args) {
        return db.jdbc.queryForList("SELECT DISTINCT p.permission_code FROM permission p JOIN role_permission rp ON p.id=rp.permission_id JOIN role r ON r.id=rp.role_id JOIN account_role_assignment ar ON ar.role_id=r.id WHERE ar.account_id=? AND ar.status='ACTIVE' AND r.is_active=true AND p.is_active=true AND ar.organization_id IS NULL AND ar.application_id IS NULL AND (ar.valid_from IS NULL OR ar.valid_from<=?) AND (ar.valid_until IS NULL OR ar.valid_until>?)",String.class,args);
    }
    public List<Map<String,Object>> findRefreshToken(Object... args) {
        return db.jdbc.queryForList("SELECT * FROM refresh_token WHERE token_hash=?",args);
    }
    public int revokeTokenFamily(Object... args) {
        return db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE family_id=?",args);
    }
    public Map<String,Object> findSession(Object... args) {
        return db.one("SELECT * FROM idp_session WHERE id=?",args);
    }
    public int consumeRefreshToken(Object... args) {
        return db.jdbc.update("UPDATE refresh_token SET consumed_at=?,status='ROTATED' WHERE id=? AND consumed_at IS NULL AND status='ACTIVE'",args);
    }
    public int linkReplacementToken(Object... args) {
        return db.jdbc.update("UPDATE refresh_token SET replaced_by_token_id=? WHERE id=?",args);
    }
    public int revokeSession(Object... args) {
        return db.jdbc.update("UPDATE idp_session SET status='REVOKED',revoked_at=? WHERE id=?",args);
    }
    public int revokeSessionTokens(Object... args) {
        return db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE session_id=?",args);
    }
    public boolean isSessionActive(String sid,String account) {
        var now=java.time.Instant.now();
        String timestamp=vn.kma.idp.shared.Time.format(now),cutoff=vn.kma.idp.shared.Time.format(now.minusSeconds(idleSeconds));
        boolean valid=db.jdbc.queryForObject("SELECT COUNT(*) FROM idp_session s JOIN account a ON s.account_id=a.id WHERE s.id=? AND a.id=? AND s.status='ACTIVE' AND s.revoked_at IS NULL AND s.expires_at>? AND s.last_activity_at>? AND a.status='ACTIVE' AND a.is_locked=false AND (a.locked_until IS NULL OR a.locked_until<=?)",Integer.class,sid,account,timestamp,cutoff,timestamp)==1;
        if(valid)db.jdbc.update("UPDATE idp_session SET last_activity_at=? WHERE id=? AND status='ACTIVE' AND last_activity_at<?",timestamp,sid,vn.kma.idp.shared.Time.format(now.minusSeconds(60)));
        return valid;
    }
    public void revokeAllAccountSessions(String account) {
        db.jdbc.update("UPDATE idp_session SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
        db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE account_id=?",Database.now(),account);
    }
}
