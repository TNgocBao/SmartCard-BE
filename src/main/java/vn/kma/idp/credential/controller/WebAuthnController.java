package vn.kma.idp.credential.controller;

import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.credential.dto.*;
import vn.kma.idp.credential.service.WebAuthnService;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.ApiException;

@RestController
public class WebAuthnController {
    private final WebAuthnService service;private final AccessService access;
    public WebAuthnController(WebAuthnService service,AccessService access){this.service=service;this.access=access;}
    @PostMapping("/api/v1/me/webauthn/registration/options") public Object options(@AuthenticationPrincipal UserPrincipal user){access.recentAal2(user);return service.registrationOptions(user);}
    @PostMapping("/api/v1/me/webauthn/registration/verify") public Object register(@AuthenticationPrincipal UserPrincipal user,@Valid @RequestBody WebAuthnRegistration proof){access.recentAal2(user);return checked(service.register(user,proof));}
    @PostMapping("/api/v1/auth/webauthn/options") public Object options(@Valid @RequestBody WebAuthnOptions request){return service.authenticationOptions(request.username());}
    @PostMapping("/api/v1/auth/webauthn/verify") public Object verify(@Valid @RequestBody WebAuthnAssertion proof){return checked(service.authenticate(proof));}
    private Map<String,Object> checked(Map<String,Object> result){if(result.containsKey("error"))throw new ApiException(401,result.get("error").toString());return result;}
}
