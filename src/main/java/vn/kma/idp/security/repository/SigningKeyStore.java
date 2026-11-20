package vn.kma.idp.security.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.*;

@Repository
public class SigningKeyStore {
    private final Database db;
    public SigningKeyStore(Database db){this.db=db;}
    public long count(){return db.jdbc.queryForObject("SELECT COUNT(*) FROM signing_key",Long.class);}
    public List<Map<String,Object>> active(){return db.jdbc.queryForList("SELECT * FROM signing_key WHERE status='ACTIVE' AND active_slot=1");}
    public Map<String,Object> lockActive(){return db.one("SELECT * FROM signing_key WHERE status='ACTIVE' AND active_slot=1 FOR UPDATE");}
    public Map<String,Object> require(String id){return db.one("SELECT * FROM signing_key WHERE id=? FOR UPDATE",id);}
    public Map<String,Object> byKid(String kid){return db.one("SELECT * FROM signing_key WHERE kid=?",kid);}
    public List<Map<String,Object>> publicKeys(){return db.jdbc.queryForList("SELECT public_jwk FROM signing_key WHERE status IN ('NEXT','ACTIVE','RETIRED') AND expires_at>? ORDER BY created_at",Database.now());}
    public List<Map<String,Object>> metadata(){return db.jdbc.queryForList("SELECT id,kid,algorithm,status,not_before,expires_at,created_at FROM signing_key ORDER BY created_at DESC LIMIT 100");}
    public int pending(){return db.jdbc.queryForObject("SELECT COUNT(*) FROM signing_key WHERE status='NEXT'",Integer.class);}
    public String insert(Map<String,Object> values){return db.insert("signing_key",values);}
    public void retire(String id,String overlapEnd){db.jdbc.update("UPDATE signing_key SET status='RETIRED',active_slot=NULL,expires_at=? WHERE id=? AND status='ACTIVE'",overlapEnd,id);}
    public void activate(String id){if(db.jdbc.update("UPDATE signing_key SET status='ACTIVE',active_slot=1,not_before=? WHERE id=? AND status='NEXT'",Database.now(),id)!=1)throw new ApiException(409,"KEY_NOT_NEXT");}
    public void revoke(String id){db.jdbc.update("UPDATE signing_key SET status='REVOKED',active_slot=NULL WHERE id=?",id);}
    public void revokeSessions(){db.jdbc.update("UPDATE idp_session SET status='REVOKED',revoked_at=? WHERE status='ACTIVE'",Database.now());db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE status='ACTIVE'",Database.now());}
    public void audit(String actor,String action,String id){db.audit(actor,action,"signing_key",id,"SUCCESS");}
}
