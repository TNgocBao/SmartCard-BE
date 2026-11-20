package vn.kma.idp.client.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.security.Keys;
@RestController
public class PublicKeyController {
    private final Keys keys;
    public PublicKeyController(Keys keys){
        this.keys=keys;
    }
    @GetMapping("/oauth2/jwks") Map<String,Object> jwks(){
        return keys.publicKeys();
    }
}
