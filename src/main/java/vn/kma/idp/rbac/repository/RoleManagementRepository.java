package vn.kma.idp.rbac.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.*;
import vn.kma.idp.rbac.domain.PermissionCatalog;

@Repository
public class RoleManagementRepository {
    private final Database db;
    public RoleManagementRepository(Database db){this.db=db;}
    public void ensureCatalog(){
        for(String code:PermissionCatalog.CODES){
            if(db.jdbc.queryForObject("SELECT COUNT(*) FROM permission WHERE permission_code=?",Integer.class,code)>0)continue;
            int separator=code.lastIndexOf('.');
            try{db.insert("permission",Map.of("permission_code",code,"resource",code.substring(0,separator),"action",code.substring(separator+1),"is_active",true));}
            catch(org.springframework.dao.DuplicateKeyException concurrentBootstrap){/* unique code wins */}
        }
    }
    public List<Map<String,Object>> permissions(){return db.jdbc.queryForList("SELECT id,permission_code,resource,action,description,is_active FROM permission WHERE is_active=true ORDER BY permission_code").stream().filter(row->PermissionCatalog.CODES.contains(row.get("permission_code"))).toList();}
    public Map<String,Object> permission(String id){return db.one("SELECT * FROM permission WHERE id=? AND is_active=true",id);}
    public void lockGovernance(){db.jdbc.queryForList("SELECT id FROM role WHERE role_code='SUPER_ADMIN' FOR UPDATE");}
    public void lockAccount(String account){db.one("SELECT id FROM account WHERE id=? FOR UPDATE",account);}
    public Map<String,Object> role(String id){return db.one("SELECT * FROM role WHERE id=?",id);}
    public List<Map<String,Object>> roles(int page,int size){return db.jdbc.queryForList("SELECT * FROM role ORDER BY role_code LIMIT ? OFFSET ?",size,page*size);}
    public List<Map<String,Object>> rolePermissions(String id){return db.jdbc.queryForList("SELECT p.id,p.permission_code FROM permission p JOIN role_permission rp ON rp.permission_id=p.id WHERE rp.role_id=? ORDER BY p.permission_code",id);}
    public String create(Map<String,Object> values){return db.insert("role",values);}
    public void update(String id,String name,String description,boolean active){db.jdbc.update("UPDATE role SET role_name=?,description=?,is_active=?,updated_at=? WHERE id=?",name,description,active,Database.now(),id);}
    public void replacePermissions(String id,List<String> permissions){db.jdbc.update("DELETE FROM role_permission WHERE role_id=?",id);for(String permission:permissions)db.insert("role_permission",Map.of("role_id",id,"permission_id",permission,"created_at",Database.now()));}
    public List<Map<String,Object>> assignments(String account){return db.jdbc.queryForList("SELECT ar.*,r.role_code FROM account_role_assignment ar JOIN role r ON r.id=ar.role_id WHERE ar.account_id=? ORDER BY ar.created_at DESC LIMIT 100",account);}
    public Map<String,Object> assignment(String id){return db.one("SELECT * FROM account_role_assignment WHERE id=? FOR UPDATE",id);}
    public boolean overlappingAssignment(String account,vn.kma.idp.rbac.dto.NewRoleAssignment request){return db.jdbc.queryForObject("SELECT COUNT(*) FROM account_role_assignment WHERE account_id=? AND role_id=? AND status='ACTIVE' AND COALESCE(organization_id,'')=COALESCE(?,'') AND COALESCE(application_id,'')=COALESCE(?,'') AND COALESCE(valid_from,'0000-01-01T00:00:00.000000000Z')<? AND COALESCE(valid_until,'9999-12-31T23:59:59.999999999Z')>?",Integer.class,account,request.roleId(),request.organizationId(),request.applicationId(),request.validUntil()==null?"9999-12-31T23:59:59.999999999Z":Time.format(request.validUntil()),request.validFrom()==null?"0000-01-01T00:00:00.000000000Z":Time.format(request.validFrom()))>0;}
    public String assign(Map<String,Object> values){return db.insert("account_role_assignment",values);}
    public void revokeAssignment(String id){db.jdbc.update("UPDATE account_role_assignment SET status='REVOKED' WHERE id=?",id);}
    public void requireScope(String org,String app){
        if(org!=null)db.one("SELECT id FROM organization WHERE id=? AND status='ACTIVE'",org);
        if(app!=null)db.one("SELECT id FROM application WHERE id=? AND status='ACTIVE'",app);
    }
    public void audit(String actor,String action,String resource,String id){db.audit(actor,action,resource,id,"SUCCESS");}
}
