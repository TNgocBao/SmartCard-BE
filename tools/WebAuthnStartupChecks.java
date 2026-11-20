import java.util.Map;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import vn.kma.idp.credential.service.WebAuthnVerifier;

/** Isolated constructor regression; no Spring Boot context, database or server is started. */
public final class WebAuthnStartupChecks {
    public static void main(String[] args)throws Exception {
        var environment=new StandardEnvironment();environment.setActiveProfiles("local");
        environment.getPropertySources().addFirst(new MapPropertySource("startup-check",Map.of("app.webauthn.rp-id","localhost","app.webauthn.origin","http://localhost:8080")));
        var verifier=new WebAuthnVerifier(environment);
        if(!verifier.rpId().equals("localhost"))throw new AssertionError("Verifier construction");
        String loaded=WebAuthnVerifier.class.getProtectionDomain().getCodeSource().getLocation().toString();
        if(!loaded.replace('\\','/').contains("/target/cached-runtime/classes/"))throw new AssertionError("Runtime loaded IDE classes");
        var mapper=new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.cbor.CBORFactory());
        byte[] encoded=mapper.writeValueAsBytes(Map.of("challenge","fixture"));
        if(!mapper.readTree(encoded).path("challenge").asText().equals("fixture"))throw new AssertionError("CBOR runtime serialization");
        environment.setActiveProfiles("prod");
        boolean rejected=false;try{new WebAuthnVerifier(environment);}catch(IllegalArgumentException expected){rejected=true;}
        if(!rejected)throw new AssertionError("Production HTTP origin was accepted");
        System.out.println("WEBAUTHN STARTUP CHECKS PASS: 4; isolated constructor/CBOR/origin checks; no Spring Boot, DB or HTTP server");
    }
}
