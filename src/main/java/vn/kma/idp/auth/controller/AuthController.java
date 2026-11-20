package vn.kma.idp.auth.controller;
import vn.kma.idp.auth.dto.*;
import vn.kma.idp.auth.service.AuthService;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.kma.idp.security.UserPrincipal;
import vn.kma.idp.shared.*;
import vn.kma.idp.security.Keys;
@RestController
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) {
        this.auth=auth;
    }
    @PostMapping("/api/v1/auth/password") Map<String,Object> password(@Valid @RequestBody Password r) {
        return auth.passwordLogin(r.username(),r.password());
    }
    @PostMapping("/api/v1/auth/smart-card/challenge") Map<String,Object> challenge(@Valid @RequestBody Challenge r) {
        return auth.challenge(r.username(),r.cardIdentifier().toUpperCase());
    }
    @PostMapping("/api/v1/auth/smart-card/verify") Map<String,Object> verify(@Valid @RequestBody Proof r) {
        return auth.verify(r.challengeId(),r.signature());
    }
    @PostMapping("/api/v1/auth/refresh") Map<String,Object> refresh(@Valid @RequestBody Refresh r) {
        var result=auth.refresh(r.refreshToken());
        if(result.containsKey("error")) throw new ApiException(401,result.get("error").toString());
        return result;
    }
}
