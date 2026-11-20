package vn.kma.idp.auth.controller;

import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.auth.dto.MfaProof;
import vn.kma.idp.auth.service.MfaService;
import vn.kma.idp.shared.ApiException;

@RestController
@RequestMapping("/api/v1/auth/mfa/totp")
public class MfaController {
    private final MfaService service;
    public MfaController(MfaService service){this.service=service;}
    @PostMapping("/verify") public Map<String,Object> verify(@Valid @RequestBody MfaProof request){
        var result=service.verify(request.transactionToken(),request.code());
        if(result.containsKey("error"))throw new ApiException(401,result.get("error").toString());return result;
    }
}
