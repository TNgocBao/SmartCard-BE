package vn.kma.idp.session.controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.security.UserPrincipal;
import vn.kma.idp.session.service.SessionService;
@RestController
@RequestMapping("/api/v1")
public class SessionController {
    private final SessionService service;
    public SessionController(SessionService service){
        this.service=service;
    }
    @GetMapping("/me") Object me(@AuthenticationPrincipal UserPrincipal user){
        return service.profile(user);
    }
    @GetMapping("/me/sessions") Object sessions(@AuthenticationPrincipal UserPrincipal user){
        return service.ownSessions(user);
    }
    @DeleteMapping("/me/sessions/{id}") void revoke(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){
        service.revoke(user,id);
    }
    @PostMapping("/logout") void logout(@AuthenticationPrincipal UserPrincipal user){
        service.logout(user);
    }
}
