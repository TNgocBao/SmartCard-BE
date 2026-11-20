package vn.kma.idp.credential.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.*;

@Repository
public class CredentialQueryRepository {
    private final Database db;
    public CredentialQueryRepository(Database db){this.db=db;}
    public List<Map<String,Object>> own(String account){return db.jdbc.queryForList("SELECT id,credential_type,credential_name,status,registered_at,last_used_at,expires_at,revoked_at FROM credential WHERE account_id=? ORDER BY registered_at DESC,id LIMIT 100",account);}
    public Map<String,Object> requireOwn(String account,String id){return db.one("SELECT id,credential_type,status FROM credential WHERE account_id=? AND id=?",account,id);}
    public void lockAccount(String account){db.one("SELECT id FROM account WHERE id=? FOR UPDATE",account);}
    public void lockSuperuserRole(){db.jdbc.queryForList("SELECT id FROM role WHERE role_code='SUPER_ADMIN' FOR UPDATE");}
    public int otherPrimary(String account,String excluding){return db.jdbc.queryForObject("SELECT COUNT(*) FROM credential WHERE account_id=? AND id<>? AND credential_type IN ('PASSWORD','SMART_CARD','WEBAUTHN') AND status='ACTIVE' AND (expires_at IS NULL OR expires_at>?)",Integer.class,account,excluding,Database.now());}
}
