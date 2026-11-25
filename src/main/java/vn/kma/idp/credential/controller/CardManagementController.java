package vn.kma.idp.credential.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.kma.idp.credential.service.CardManagementService;
import vn.kma.idp.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/me/smart-cards")
public class CardManagementController {
    private final CardManagementService service;
    public CardManagementController(CardManagementService service){this.service=service;}
    @GetMapping public Object cards(@AuthenticationPrincipal UserPrincipal user,@RequestParam(required=false) String accountId,@RequestParam(defaultValue="0") int page){return service.list(user,accountId,page,false);}
    @GetMapping("/history") public Object history(@AuthenticationPrincipal UserPrincipal user,@RequestParam(required=false) String accountId,@RequestParam(defaultValue="0") int page){return service.list(user,accountId,page,true);}
}
