package vn.kma.idp.security;
import java.util.Map;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import vn.kma.idp.security.service.SigningKeyService;
/** Token crypto facade; lifecycle, custody and persistence are separate responsibilities. */
@Component
public class Keys {
    public final String issuer;
    private final SigningKeyService signing;
    public Keys(Environment env,SigningKeyService signing){issuer=env.getRequiredProperty("app.issuer");this.signing=signing;}
    public RSAKey active(){return signing.active();}
    public RSAKey verification(String kid){return signing.verification(kid);}
    public Map<String,Object> publicKeys(){return signing.jwks();}
}
