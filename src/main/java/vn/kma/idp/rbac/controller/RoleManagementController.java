package vn.kma.idp.rbac.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.rbac.dto.*;
import vn.kma.idp.rbac.service.RoleManagementService;
import vn.kma.idp.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1")
public class RoleManagementController {
    private final RoleManagementService service;
    public RoleManagementController(RoleManagementService service){this.service=service;}
    @GetMapping("/permissions") public Object permissions(@AuthenticationPrincipal UserPrincipal user){return service.permissions(user);}
    @GetMapping("/roles") public Object roles(@AuthenticationPrincipal UserPrincipal user,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size){return service.list(user,page,size);}
    @GetMapping("/roles/{id}") public Object role(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){return service.get(user,id);}
    @PostMapping("/roles") @ResponseStatus(HttpStatus.CREATED) public Object create(@AuthenticationPrincipal UserPrincipal user,@Valid @RequestBody NewRole request){return service.create(user,request);}
    @PatchMapping("/roles/{id}") public void update(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id,@Valid @RequestBody UpdateRole request){service.update(user,id,request);}
    @PutMapping("/roles/{id}/permissions") public void permissions(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id,@Valid @RequestBody RolePermissions request){service.permissions(user,id,request);}
    @GetMapping("/accounts/{account}/role-assignments") public Object assignments(@AuthenticationPrincipal UserPrincipal user,@PathVariable String account){return service.assignments(user,account);}
    @PostMapping("/accounts/{account}/role-assignments") @ResponseStatus(HttpStatus.CREATED) public Object assign(@AuthenticationPrincipal UserPrincipal user,@PathVariable String account,@Valid @RequestBody NewRoleAssignment request){return service.assign(user,account,request);}
    @DeleteMapping("/role-assignments/{id}") public void revoke(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){service.revoke(user,id);}
}
