package vn.kma.idp.rbac.service;

import java.util.*;
import vn.kma.idp.rbac.domain.PermissionCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.rbac.dto.*;
import vn.kma.idp.rbac.repository.RoleManagementRepository;
import vn.kma.idp.account.service.AccountService;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.*;

@Service
public class RoleManagementService {
    private final RoleManagementRepository repository;private final AccessService access;private final AccountService accounts;
    public RoleManagementService(RoleManagementRepository repository,AccessService access,AccountService accounts){this.repository=repository;this.access=access;this.accounts=accounts;}
    public Object permissions(UserPrincipal user){access.require(user,"permission.read");return Map.of("data",repository.permissions());}
    public Object list(UserPrincipal user,int page,int size){access.require(user,"role.read");if(page<0||page>100000||size<1||size>100)throw new ApiException(400,"INVALID_PAGE");return Map.of("data",repository.roles(page,size),"page",page,"size",size);}
    public Object get(UserPrincipal user,String id){access.require(user,"role.read");return Map.of("data",repository.role(id),"permissions",repository.rolePermissions(id));}
    private void mutable(Map<String,Object> role){if(Boolean.TRUE.equals(role.get("is_system")))throw new ApiException(409,"SYSTEM_ROLE_PROTECTED");}
    private void editableBy(UserPrincipal user,String id){mutable(repository.role(id));for(var permission:repository.rolePermissions(id))if(!access.has(user,permission.get("permission_code").toString()))throw new ApiException(403,"PRIVILEGE_ESCALATION_DENIED");}
    @Transactional public Object create(UserPrincipal user,NewRole request){
        access.require(user,"role.create");access.recentAal2(user);repository.lockGovernance();
        if(request.code().equals("SUPER_ADMIN"))throw new ApiException(409,"SYSTEM_ROLE_PROTECTED");
        var fields=new LinkedHashMap<String,Object>();fields.put("role_code",request.code());fields.put("role_name",request.name());fields.put("description",request.description());fields.put("is_system",false);fields.put("is_active",true);fields.put("created_at",Database.now());
        String id=repository.create(fields);repository.audit(user.accountId(),"role.created","role",id);return Map.of("id",id);
    }
    @Transactional public void update(UserPrincipal user,String id,UpdateRole request){
        access.require(user,"role.update");access.recentAal2(user);repository.lockGovernance();var role=repository.role(id);editableBy(user,id);
        repository.update(id,request.name()==null?Objects.toString(role.get("role_name"),""):request.name(),request.description()==null?Objects.toString(role.get("description"),""):request.description(),request.active()==null?Boolean.TRUE.equals(role.get("is_active")):request.active());repository.audit(user.accountId(),"role.updated","role",id);
    }
    @Transactional public void permissions(UserPrincipal user,String id,RolePermissions request){
        access.require(user,"role.update");access.recentAal2(user);repository.lockGovernance();editableBy(user,id);
        List<String> ids=request.permissionIds().stream().distinct().toList();if(ids.size()!=request.permissionIds().size())throw new ApiException(400,"DUPLICATE_PERMISSIONS");
        for(String permission:ids){String code=repository.permission(permission).get("permission_code").toString();if(!PermissionCatalog.CODES.contains(code))throw new ApiException(400,"PERMISSION_NOT_ENFORCED");if(!access.has(user,code))throw new ApiException(403,"PRIVILEGE_ESCALATION_DENIED");}
        repository.replacePermissions(id,ids);repository.audit(user.accountId(),"role.permissions_replaced","role",id);
    }
    public Object assignments(UserPrincipal user,String account){access.require(user,"role.read");return Map.of("data",repository.assignments(account));}
    @Transactional public Object assign(UserPrincipal user,String account,NewRoleAssignment request){
        if(request.organizationId()!=null&&request.applicationId()!=null)throw new ApiException(400,"AMBIGUOUS_ROLE_SCOPE");
        if(!access.has(user,"role.assign",request.organizationId(),request.applicationId()))throw ApiException.denied();access.recentAal2(user);
        repository.lockGovernance();repository.lockAccount(account);repository.requireScope(request.organizationId(),request.applicationId());var role=repository.role(request.roleId());
        if(!Boolean.TRUE.equals(role.get("is_active")))throw new ApiException(409,"ROLE_INACTIVE");
        if(request.validFrom()!=null&&request.validUntil()!=null&&!request.validUntil().isAfter(request.validFrom()))throw new ApiException(400,"INVALID_VALIDITY_INTERVAL");
        if(request.validUntil()!=null&&!request.validUntil().isAfter(java.time.Instant.now()))throw new ApiException(400,"INVALID_VALIDITY_INTERVAL");
        if("SUPER_ADMIN".equals(role.get("role_code"))&&(request.organizationId()!=null||request.applicationId()!=null||!access.has(user,"idp.manage")))throw new ApiException(403,"SYSTEM_ROLE_PROTECTED");
        for(var permission:repository.rolePermissions(request.roleId()))if(!access.has(user,permission.get("permission_code").toString(),request.organizationId(),request.applicationId()))throw new ApiException(403,"PRIVILEGE_ESCALATION_DENIED");
        if(repository.overlappingAssignment(account,request))throw new ApiException(409,"ROLE_ASSIGNMENT_OVERLAP");
        var values=new LinkedHashMap<String,Object>();values.put("account_id",account);values.put("role_id",request.roleId());values.put("organization_id",request.organizationId());values.put("application_id",request.applicationId());values.put("assigned_by",user.accountId());values.put("status","ACTIVE");values.put("created_at",Database.now());if(request.validFrom()!=null)values.put("valid_from",Time.format(request.validFrom()));if(request.validUntil()!=null)values.put("valid_until",Time.format(request.validUntil()));
        String id=repository.assign(values);repository.audit(user.accountId(),"role.assigned","account_role_assignment",id);return Map.of("id",id);
    }
    @Transactional public void revoke(UserPrincipal user,String id){
        repository.lockGovernance();var assignment=repository.assignment(id);
        if(!access.has(user,"role.assign",(String)assignment.get("organization_id"),(String)assignment.get("application_id")))throw ApiException.denied();access.recentAal2(user);
        String account=assignment.get("account_id").toString();repository.lockAccount(account);
        if("SUPER_ADMIN".equals(repository.role(assignment.get("role_id").toString()).get("role_code")))accounts.protectLastAdmin(account);
        repository.revokeAssignment(id);repository.audit(user.accountId(),"role.assignment_revoked","account_role_assignment",id);
    }
}
