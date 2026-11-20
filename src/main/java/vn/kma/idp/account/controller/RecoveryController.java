package vn.kma.idp.account.controller;

import java.util.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.account.dto.*;
import vn.kma.idp.account.service.*;
import vn.kma.idp.credential.service.TotpService;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.ApiException;

@RestController
@RequestMapping("/api/v1")
public class RecoveryController {
    private final RecoveryService recovery;private final EnrollmentService enrollment;private final AccessService access;private final TotpService totp;private final boolean lab;
    public RecoveryController(RecoveryService recovery,EnrollmentService enrollment,AccessService access,TotpService totp,Environment env){this.recovery=recovery;this.enrollment=enrollment;this.access=access;this.totp=totp;lab=env.matchesProfiles("local","test");}
    @PostMapping("/accounts/{id}/activation-invite") @ResponseStatus(HttpStatus.ACCEPTED)
    public Object invite(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){
        access.requireAccount(user,"account.activate_invite",id);
        if(!(lab && id.equals(user.accountId()) && !totp.enrolled(id)))access.recentAal2(user);
        return recovery.invite(user.accountId(),id);
    }
    @PostMapping("/accounts/{id}/mfa/reset") public Object reset(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id,@Valid @RequestBody ResetMfa request){access.requireAccount(user,"account.mfa.reset",id);access.recentAal2(user);return recovery.resetMfa(user.accountId(),id,request.reason());}
    @PostMapping("/auth/activate") public Object activate(@Valid @RequestBody ActivationProof request){return recovery.activate(request.token());}
    @PostMapping("/auth/enrollment/totp/setup") public Object setup(@Valid @RequestBody ActivationProof request){return enrollment.setup(request.token());}
    @PostMapping("/auth/enrollment/totp/verify") public Object verify(@Valid @RequestBody EnrollmentProof request){if(request.code()==null)throw new ApiException(400,"VALIDATION_ERROR");return result(enrollment.verify(request.enrollmentToken(),request.code()));}
    @PostMapping("/auth/recovery/verify") public Object recover(@Valid @RequestBody RecoveryProof request){return result(recovery.recover(request.username(),request.recoveryCode()));}
    @PostMapping("/me/recovery-codes/regenerate") public Object codes(@AuthenticationPrincipal UserPrincipal user){access.recentAal2(user);return Map.of("oneTimeRecoveryCodes",recovery.regenerate(user.accountId()));}
    private Map<String,Object> result(Map<String,Object> value){if(value.containsKey("error"))throw new ApiException(401,value.get("error").toString());return value;}
}
