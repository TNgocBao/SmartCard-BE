package vn.kma.idp.credential.service;

import java.net.URI;
import java.util.*;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestedCredentialDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.statement.NoneAttestationStatement;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import vn.kma.idp.credential.dto.*;

/** Validates ceremonies with a dedicated verifier; this policy accepts privacy-preserving none attestation only. */
@Component
public class WebAuthnVerifier {
    private static ObjectConverter boundedConverter(){
        var constraints=com.fasterxml.jackson.core.StreamReadConstraints.builder().maxNestingDepth(16).maxStringLength(32768).maxNumberLength(64).build();
        var jsonFactory=com.fasterxml.jackson.core.JsonFactory.builder().streamReadConstraints(constraints).enable(com.fasterxml.jackson.core.StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
        var cborFactory=com.fasterxml.jackson.dataformat.cbor.CBORFactory.builder().streamReadConstraints(constraints).build();
        return new ObjectConverter(new com.fasterxml.jackson.databind.ObjectMapper(jsonFactory),new com.fasterxml.jackson.databind.ObjectMapper(cborFactory));
    }
    private final ObjectConverter converter=boundedConverter();
    private final WebAuthnManager manager=WebAuthnManager.createNonStrictWebAuthnManager(converter);
    private final AttestedCredentialDataConverter acd=new AttestedCredentialDataConverter(converter);
    private final String rpId,origin;
    private final List<PublicKeyCredentialParameters> algorithms=List.of(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.ES256));
    public WebAuthnVerifier(Environment env){
        rpId=env.getProperty("app.webauthn.rp-id","localhost");origin=env.getProperty("app.webauthn.origin","http://localhost:8080");
        URI uri=URI.create(origin);String host=uri.getHost();
        if(host==null||uri.getRawUserInfo()!=null||uri.getRawQuery()!=null||uri.getRawFragment()!=null||(!uri.getPath().isEmpty()&&!uri.getPath().equals("/"))||!(host.equals(rpId)||host.endsWith("."+rpId))||!("https".equals(uri.getScheme())||"http".equals(uri.getScheme())&&host.equals("localhost")))throw new IllegalArgumentException("Invalid WebAuthn RP/origin configuration");
        if(env.matchesProfiles("prod")&&!"https".equals(uri.getScheme()))throw new IllegalArgumentException("Production WebAuthn requires HTTPS origin");
    }
    public String rpId(){return rpId;}
    private ServerProperty server(String challenge){return new ServerProperty(new Origin(origin),rpId,new DefaultChallenge(decode(challenge)),null);}
    public Map<String,Object> registration(String challenge,String handle,WebAuthnRegistration proof){
        var data=manager.parse(new RegistrationRequest(decode(proof.attestationObject()),decode(proof.clientDataJSON())));
        if(!"none".equals(data.getAttestationObject().getAttestationStatement().getFormat()))throw new IllegalArgumentException("Unsupported attestation policy");
        manager.validate(data,new RegistrationParameters(server(challenge),algorithms,true,true));
        var record=new CredentialRecordImpl(data.getAttestationObject(),data.getCollectedClientData(),data.getClientExtensions(),data.getTransports());
        var attested=record.getAttestedCredentialData();String id=encode(attested.getCredentialId());
        return Map.ofEntries(Map.entry("webauthn_credential_id",id),Map.entry("credential_id_hash",vn.kma.idp.auth.service.AuthService.hash(id)),Map.entry("user_handle",handle),Map.entry("rp_id",rpId),Map.entry("public_key_cose",Base64.getEncoder().encodeToString(converter.getCborConverter().writeValueAsBytes(attested.getCOSEKey()))),Map.entry("attested_credential_data",Base64.getEncoder().encodeToString(acd.convert(attested))),Map.entry("cose_algorithm",-7),Map.entry("sign_count",record.getCounter()),Map.entry("aaguid",attested.getAaguid().toString()),Map.entry("backup_eligible",Boolean.TRUE.equals(record.isBackupEligible())),Map.entry("backup_state",Boolean.TRUE.equals(record.isBackedUp())),Map.entry("attestation_format","none"));
    }
    public record Verified(long counter,boolean backedUp){}
    public Verified assertion(String challenge,Map<String,Object> stored,WebAuthnAssertion proof){
        String normalized=encode(decode(proof.credentialId()));
        if(!normalized.equals(stored.get("webauthn_credential_id"))||!rpId.equals(stored.get("rp_id")))throw new IllegalArgumentException();
        if(proof.userHandle()!=null&&!proof.userHandle().equals(stored.get("user_handle")))throw new IllegalArgumentException();
        long old=((Number)stored.get("sign_count")).longValue();
        var record=new CredentialRecordImpl(new NoneAttestationStatement(),true,Boolean.TRUE.equals(stored.get("backup_eligible")),Boolean.TRUE.equals(stored.get("backup_state")),old,acd.convert(Base64.getDecoder().decode(stored.get("attested_credential_data").toString())),null,null,null,Set.of());
        var data=manager.parse(new AuthenticationRequest(decode(proof.credentialId()),proof.userHandle()==null?null:decode(proof.userHandle()),decode(proof.authenticatorData()),decode(proof.clientDataJSON()),null,decode(proof.signature())));
        manager.validate(data,new AuthenticationParameters(server(challenge),record,List.of(decode(normalized)),true,true));
        long counter=data.getAuthenticatorData().getSignCount();
        if((old!=0||counter!=0)&&counter<=old)throw new IllegalArgumentException("Authenticator counter did not advance");
        return new Verified(counter,data.getAuthenticatorData().isFlagBS());
    }
    public static byte[] decode(String value){return Base64.getUrlDecoder().decode(value);}
    public static String encode(byte[] value){return Base64.getUrlEncoder().withoutPadding().encodeToString(value);}
}
