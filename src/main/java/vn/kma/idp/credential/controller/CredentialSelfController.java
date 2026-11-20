package vn.kma.idp.credential.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.kma.idp.credential.service.CredentialSelfService;
import vn.kma.idp.security.*;

@RestController
@RequestMapping("/api/v1/me/credentials")
public class CredentialSelfController {
    private final CredentialSelfService service;private final AccessService access;
    public CredentialSelfController(CredentialSelfService service,AccessService access){this.service=service;this.access=access;}
    @GetMapping public Object list(@AuthenticationPrincipal UserPrincipal user){return service.list(user);}
    @DeleteMapping("/{id}") public void revoke(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){access.recentAal2(user);service.revoke(user,id);}
}
