package vn.kma.idp.audit.controller;
import java.util.*;
import org.springframework.core.env.Environment;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.audit.repository.OutboxStore;
import vn.kma.idp.audit.service.OutboxService;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.ApiException;

/** Explicit local-only test inbox. Never enabled by a production profile or returned by public OTP APIs. */
@RestController
@RequestMapping("/api/v1/local-mailbox")
public class LocalMailboxController {
    private final OutboxStore repository;private final OutboxService service;private final AccessService access;private final boolean lab;
    public LocalMailboxController(OutboxStore repository,OutboxService service,AccessService access,Environment env){this.repository=repository;this.service=service;this.access=access;lab=env.matchesProfiles("local","test")&&!env.matchesProfiles("prod");}
    @GetMapping("/{id}") public Object read(@AuthenticationPrincipal UserPrincipal user,@PathVariable String id){
        if(!lab)throw new ApiException(404,"RESOURCE_NOT_FOUND");access.require(user,"idp.manage");
        var item=repository.get(id);return Map.of("data",service.decrypt(id,item.get("payload").toString()),"localTestOnly",true);
    }
}
