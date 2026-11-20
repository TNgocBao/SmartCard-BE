package vn.kma.idp.account.controller;
import vn.kma.idp.account.dto.*;
import vn.kma.idp.account.service.AccountService;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.kma.idp.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accounts;
    private final vn.kma.idp.security.AccessService access;
    public AccountController(AccountService accounts,vn.kma.idp.security.AccessService access) {
        this.accounts=accounts;this.access=access;
    }
    @PostMapping Map<String,Object> create(@AuthenticationPrincipal UserPrincipal jwt,@Valid @RequestBody NewAccount r) {
        access.require(jwt,"account.create");
        return accounts.onboard(jwt.accountId(),r.username(),r.displayName(),r.email()==null?"":r.email());
    }
    @PostMapping("/{id}/smart-cards") Map<String,Object> enroll(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String id,@Valid @RequestBody Card r) {
        access.requireAccount(jwt,"credential.manage",id);
        return accounts.enroll(jwt.accountId(),id,r.cardIdentifier().toUpperCase(),r.publicKeySpki(),r.transport());
    }
    @PostMapping("/{id}/lock") void lock(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String id) {
        access.requireAccount(jwt,"account.lock",id);
        accounts.lock(jwt.accountId(),id,true);
    }
    @PostMapping("/{id}/unlock") void unlock(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String id) {
        access.requireAccount(jwt,"account.unlock",id);
        accounts.lock(jwt.accountId(),id,false);
    }
    @DeleteMapping("/{id}/sessions") void sessions(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String id) {
        access.requireAccount(jwt,"session.revoke",id);
        accounts.forceLogout(jwt.accountId(),id);
    }
    @DeleteMapping("/credentials/{id}") void credential(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String id) {
        access.require(jwt,"credential.manage");
        accounts.revokeCredential(jwt.accountId(),id);
    }
}
