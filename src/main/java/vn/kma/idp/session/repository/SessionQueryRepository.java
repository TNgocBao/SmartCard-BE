package vn.kma.idp.session.repository;
import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;
@Repository
public class SessionQueryRepository {
    private final Database db;
    public SessionQueryRepository(Database db){
        this.db=db;
    }
    public Map<String,Object> profile(String account){
        return db.one("SELECT a.id,a.username,a.status,p.display_name,p.email FROM account a JOIN identity i ON a.identity_id=i.id JOIN person p ON i.person_id=p.id WHERE a.id=?",account);
    }
    public List<Map<String,Object>> ownSessions(String account){
        return db.jdbc.queryForList("SELECT id,status,amr,authenticated_at,expires_at FROM idp_session WHERE account_id=? ORDER BY authenticated_at DESC",account);
    }
    public void requireOwnership(String account,String sid){
        db.one("SELECT id FROM idp_session WHERE id=? AND account_id=?",sid,account);
    }
    public void audit(String account,String action,String sid){
        db.audit(account,action,"idp_session",sid,"SUCCESS");
    }
}
