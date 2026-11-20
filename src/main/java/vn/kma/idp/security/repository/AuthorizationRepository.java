package vn.kma.idp.security.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;

@Repository
public class AuthorizationRepository {
    private final Database db;
    public AuthorizationRepository(Database db){this.db=db;}
    public boolean has(String account,String permission,String organization,String application) {
        return db.jdbc.queryForObject("SELECT COUNT(*) FROM account_role_assignment ar JOIN role r ON ar.role_id=r.id JOIN role_permission rp ON r.id=rp.role_id JOIN permission p ON p.id=rp.permission_id WHERE ar.account_id=? AND p.permission_code=? AND ar.status='ACTIVE' AND r.is_active=true AND p.is_active=true AND (ar.valid_from IS NULL OR ar.valid_from<=?) AND (ar.valid_until IS NULL OR ar.valid_until>?) AND (ar.organization_id IS NULL OR ar.organization_id=?) AND (ar.application_id IS NULL OR ar.application_id=?)",Integer.class,account,permission,Database.now(),Database.now(),organization,application)>0;
    }
    public Map<String,Object> session(String account,String sid){return db.one("SELECT * FROM idp_session WHERE account_id=? AND id=? AND status='ACTIVE' AND revoked_at IS NULL",account,sid);}
}
