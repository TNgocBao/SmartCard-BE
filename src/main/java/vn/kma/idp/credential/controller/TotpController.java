package vn.kma.idp.credential.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.credential.dto.TotpConfirmation;
import vn.kma.idp.credential.service.TotpService;
import vn.kma.idp.account.service.AccountService;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.ApiException;

@RestController
@RequestMapping("/api/v1/me/totp")
public class TotpController {
    private final TotpService service;private final AccessService access;private final AccountService accounts;
    public TotpController(TotpService service,AccessService access,AccountService accounts){this.service=service;this.access=access;this.accounts=accounts;}
    @PostMapping("/setup") public Object setup(@AuthenticationPrincipal UserPrincipal user){access.recentAal2(user);return service.setup(user.accountId());}
    @PostMapping("/verify") public Object verify(@AuthenticationPrincipal UserPrincipal user,@Valid @RequestBody TotpConfirmation request){access.recentAal2(user);if(!service.confirm(user.accountId(),request.credentialId(),request.code()))throw new ApiException(401,"AUTHENTICATION_FAILED");return java.util.Map.of("status","ACTIVE");}
    @DeleteMapping public void disable(@AuthenticationPrincipal UserPrincipal user){access.recentAal2(user);service.disable(user.accountId());accounts.revokeAll(user.accountId());}
}
