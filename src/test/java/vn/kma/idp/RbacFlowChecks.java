package vn.kma.idp;

import java.util.*;
import java.util.function.BiConsumer;
import com.fasterxml.jackson.databind.JsonNode;
import vn.kma.idp.auth.service.AuthService;
import vn.kma.idp.shared.Database;

/** Canonical RBAC API tests. Authenticated fixtures isolate authorization from the separate MFA suite. */
final class RbacFlowChecks {
    @FunctionalInterface interface Request {JsonNode send(String method,String path,Object body,String token,int expected)throws Exception;}
    private final Request http;
    private final Database db;
    private final AuthService auth;
    private final BiConsumer<String,Boolean> check;
    RbacFlowChecks(Request http,Database db,AuthService auth,BiConsumer<String,Boolean> check){this.http=http;this.db=db;this.auth=auth;this.check=check;}
    void run(String admin)throws Exception {
        String member=db.one("SELECT id FROM account WHERE username='student01'").get("id").toString();
        String adminId=http.send("GET","/api/v1/me",null,admin,200).path("data").path("id").asText();
        var permissions=http.send("GET","/api/v1/permissions",null,admin,200).path("data");
        check.accept("Permission catalog offers actual enforced codes only",permissions.size()==vn.kma.idp.rbac.domain.PermissionCatalog.CODES.size());
        Map<String,String> ids=new HashMap<>();
        for(var permission:permissions)ids.put(permission.path("permission_code").asText(),permission.path("id").asText());
        http.send("GET","/api/v1/roles?size=101",null,admin,400);
        String role=http.send("POST","/api/v1/roles",Map.of("code","HELPDESK_TEST","name","Helpdesk test"),admin,201).path("id").asText();
        http.send("POST","/api/v1/roles",Map.of("code","HELPDESK_TEST","name","Duplicate"),admin,409);
        String superRole=db.one("SELECT id FROM role WHERE role_code='SUPER_ADMIN'").get("id").toString();
        http.send("PATCH","/api/v1/roles/"+superRole,Map.of("active",false),admin,409);
        http.send("PUT","/api/v1/roles/"+superRole+"/permissions",Map.of("permissionIds",List.of()),admin,409);
        var permissionIds=List.of(ids.get("role.read"),ids.get("role.update"),ids.get("role.assign"),ids.get("account.lock"),ids.get("account.unlock"),ids.get("credential.manage"));
        http.send("PUT","/api/v1/roles/"+role+"/permissions",Map.of("permissionIds",permissionIds),admin,200);
        http.send("PUT","/api/v1/roles/"+role+"/permissions",Map.of("permissionIds",List.of(ids.get("account.lock"),ids.get("account.lock"))),admin,400);
        String assignment=http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",role),admin,201).path("id").asText();
        http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",role),admin,409);
        http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",role,"organizationId",Database.id(),"applicationId",Database.id()),admin,400);
        http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",role,"validFrom","2030-01-01T00:00:00Z","validUntil","2029-01-01T00:00:00Z"),admin,400);
        String delegate=auth.createSession(member,"WEBAUTHN_UV").get("accessToken").toString();
        http.send("GET","/api/v1/roles",null,delegate,200);
        http.send("POST","/api/v1/roles",Map.of("code","UNAUTHORIZED_ROLE","name","Denied"),delegate,403);
        http.send("PUT","/api/v1/roles/"+role+"/permissions",Map.of("permissionIds",List.of(ids.get("idp.manage"))),delegate,403);
        http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",superRole),delegate,403);
        http.send("POST","/api/v1/accounts/"+adminId+"/lock",Map.of(),delegate,403);
        String adminTotp=db.one("SELECT id FROM credential WHERE account_id=? AND credential_type='TOTP' AND status='ACTIVE'",adminId).get("id").toString();
        http.send("DELETE","/api/v1/accounts/credentials/"+adminTotp,null,delegate,403);
        String target=http.send("POST","/api/v1/accounts",Map.of("username","rbac-target","displayName","Scoped target","email","target@example.test"),admin,200).path("accountId").asText();
        http.send("POST","/api/v1/accounts/"+target+"/lock",Map.of(),delegate,200);
        http.send("POST","/api/v1/accounts/"+target+"/unlock",Map.of(),delegate,200);
        String elevated=http.send("POST","/api/v1/roles",Map.of("code","ELEVATED_TEST","name","Elevated"),admin,201).path("id").asText();
        http.send("PUT","/api/v1/roles/"+elevated+"/permissions",Map.of("permissionIds",List.of(ids.get("idp.manage"))),admin,200);
        http.send("PATCH","/api/v1/roles/"+elevated,Map.of("active",false),delegate,403);
        http.send("POST","/api/v1/accounts/"+target+"/role-assignments",Map.of("roleId",elevated),delegate,403);
        http.send("DELETE","/api/v1/role-assignments/"+assignment,null,admin,200);
        http.send("POST","/api/v1/accounts/"+target+"/lock",Map.of(),delegate,403);
        check.accept("Role revocation affects an existing token immediately",!http.send("GET","/api/v1/me",null,delegate,200).path("permissions").toString().contains("account.lock"));
        String superAssignment=db.one("SELECT id FROM account_role_assignment WHERE account_id=? AND role_id=? AND status='ACTIVE'",adminId,superRole).get("id").toString();
        http.send("DELETE","/api/v1/role-assignments/"+superAssignment,null,admin,409);
        scopedAssignments(admin,member,role,superRole,adminId);
    }
    private void scopedAssignments(String admin,String member,String role,String superRole,String adminId)throws Exception {
        String organization=db.insert("organization",Map.of("org_code","RBAC_SCOPE","status","ACTIVE"));
        String application=db.insert("application",Map.of("client_id","rbac-scope-app","client_type","PUBLIC","require_pkce",true,"status","ACTIVE"));
        String scoped=http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",role,"organizationId",organization),admin,201).path("id").asText();
        var repo=new vn.kma.idp.security.repository.AuthorizationRepository(db);
        check.accept("Canonical scoped assignment cannot grant global rights",!repo.has(member,"account.lock",null,null));
        check.accept("Canonical scoped assignment matches its own organization",repo.has(member,"account.lock",organization,null));
        check.accept("Organization grant cannot cross into application scope",!repo.has(member,"account.lock",null,application));
        String delegated=auth.createSession(member,"WEBAUTHN_UV").get("accessToken").toString();
        http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",role,"applicationId",application),delegated,403);
        http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",superRole,"organizationId",organization),admin,403);
        http.send("DELETE","/api/v1/role-assignments/"+scoped,null,admin,200);
        String future=http.send("POST","/api/v1/accounts/"+member+"/role-assignments",Map.of("roleId",superRole,"validFrom","2090-01-01T00:00:00Z"),admin,201).path("id").asText();
        http.send("POST","/api/v1/accounts/"+adminId+"/lock",Map.of(),admin,409);
        http.send("DELETE","/api/v1/role-assignments/"+future,null,admin,200);
    }
}
