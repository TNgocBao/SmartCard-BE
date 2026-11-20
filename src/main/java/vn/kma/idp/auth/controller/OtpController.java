package vn.kma.idp.auth.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.auth.dto.*;
import vn.kma.idp.auth.service.OtpService;
import vn.kma.idp.shared.ApiException;

@RestController
@RequestMapping("/api/v1/auth/otp")
public class OtpController {
    private final OtpService service;
    public OtpController(OtpService service){this.service=service;}
    @PostMapping("/request") public Object request(@Valid @RequestBody OtpRequest request){return service.request(request.username());}
    @PostMapping("/verify") public Object verify(@Valid @RequestBody MfaProof request){var result=service.verify(request.transactionToken(),request.code());if(result.containsKey("error"))throw new ApiException(401,result.get("error").toString());return result;}
}
