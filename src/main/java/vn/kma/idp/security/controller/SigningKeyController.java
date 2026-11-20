package vn.kma.idp.security.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.security.*;
import vn.kma.idp.security.dto.*;
import vn.kma.idp.security.service.SigningKeyService;

@RestController
@RequestMapping("/api/v1/signing-keys")
public class SigningKeyController {
    private final SigningKeyService service;private final AccessService access;
    public SigningKeyController(SigningKeyService service,AccessService access){this.service=service;this.access=access;}
    @GetMapping public Object list(@AuthenticationPrincipal UserPrincipal user){access.require(user,"key.read");return service.list();}
    @PostMapping("/prepare") @ResponseStatus(HttpStatus.CREATED) public Object prepare(@AuthenticationPrincipal UserPrincipal user,@Valid @RequestBody PrepareSigningKey request){access.require(user,"key.rotate");access.recentAal2(user);return service.prepare(user.accountId(),request.alias());}
    @PostMapping("/rotate") @ResponseStatus(HttpStatus.CREATED) public Object rotate(@AuthenticationPrincipal UserPrincipal user,@Valid @RequestBody RotateSigningKey request){access.require(user,"key.rotate");access.recentAal2(user);return service.rotate(user.accountId(),request.nextKeyId(),request.overlapSeconds());}
    @DeleteMapping("/{id}") public void revoke(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){access.require(user,"key.rotate");access.recentAal2(user);service.revoke(user.accountId(),id);}
}
