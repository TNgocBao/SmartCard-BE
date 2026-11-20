package vn.kma.idp.credential.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "webauthn_credential")
public class WebauthnCredential {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "credential_id", length = 36)
    private String credentialId;
    @Column(name = "webauthn_credential_id", columnDefinition = "TEXT")
    private String webauthnCredentialId;
    @Column(name = "user_handle", columnDefinition = "TEXT")
    private String userHandle;
    @Column(name = "rp_id")
    private String rpId;
    @Column(name = "public_key_cose", columnDefinition = "TEXT")
    private String publicKeyCose;
    @Column(name = "cose_algorithm")
    private Integer coseAlgorithm;
    @Column(name = "sign_count")
    private Long signCount;
    @Column(name = "aaguid")
    private String aaguid;
    @Column(name = "transports", columnDefinition = "TEXT")
    private String transports;
    @Column(name = "backup_eligible")
    private Boolean backupEligible;
    @Column(name = "backup_state")
    private Boolean backupState;
    @Column(name = "attestation_format")
    private String attestationFormat;
    @Column(name="attested_credential_data",columnDefinition="TEXT")
    private String attestedCredentialData;
    @Column(name="credential_id_hash",length=64)
    private String credentialIdHash;
    public String getAttestedCredentialData(){return attestedCredentialData;}
    public void setAttestedCredentialData(String value){attestedCredentialData=value;}
    public String getCredentialIdHash(){return credentialIdHash;}
    public void setCredentialIdHash(String value){credentialIdHash=value;}
    public WebauthnCredential() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getCredentialId() {
        return credentialId;
    }
    public void setCredentialId(String value) {
        this.credentialId = value;
    }
    public String getWebauthnCredentialId() {
        return webauthnCredentialId;
    }
    public void setWebauthnCredentialId(String value) {
        this.webauthnCredentialId = value;
    }
    public String getUserHandle() {
        return userHandle;
    }
    public void setUserHandle(String value) {
        this.userHandle = value;
    }
    public String getRpId() {
        return rpId;
    }
    public void setRpId(String value) {
        this.rpId = value;
    }
    public String getPublicKeyCose() {
        return publicKeyCose;
    }
    public void setPublicKeyCose(String value) {
        this.publicKeyCose = value;
    }
    public Integer getCoseAlgorithm() {
        return coseAlgorithm;
    }
    public void setCoseAlgorithm(Integer value) {
        this.coseAlgorithm = value;
    }
    public Long getSignCount() {
        return signCount;
    }
    public void setSignCount(Long value) {
        this.signCount = value;
    }
    public String getAaguid() {
        return aaguid;
    }
    public void setAaguid(String value) {
        this.aaguid = value;
    }
    public String getTransports() {
        return transports;
    }
    public void setTransports(String value) {
        this.transports = value;
    }
    public Boolean getBackupEligible() {
        return backupEligible;
    }
    public void setBackupEligible(Boolean value) {
        this.backupEligible = value;
    }
    public Boolean getBackupState() {
        return backupState;
    }
    public void setBackupState(Boolean value) {
        this.backupState = value;
    }
    public String getAttestationFormat() {
        return attestationFormat;
    }
    public void setAttestationFormat(String value) {
        this.attestationFormat = value;
    }
}
