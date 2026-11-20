package vn.kma.idp.credential.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "totp_credential")
public class TotpCredential {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "credential_id", length = 36)
    private String credentialId;
    @Column(name = "encrypted_secret", columnDefinition = "TEXT")
    private String encryptedSecret;
    @Column(name = "encryption_key_id")
    private String encryptionKeyId;
    @Column(name = "algorithm")
    private String algorithm;
    @Column(name = "digits")
    private Integer digits;
    @Column(name = "period_seconds")
    private Integer periodSeconds;
    @Column(name = "verified_at", length = 40)
    private String verifiedAt;
    @Column(name = "last_accepted_step")
    private Long lastAcceptedStep;
    public TotpCredential() {
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
    public String getEncryptedSecret() {
        return encryptedSecret;
    }
    public void setEncryptedSecret(String value) {
        this.encryptedSecret = value;
    }
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }
    public void setEncryptionKeyId(String value) {
        this.encryptionKeyId = value;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String value) {
        this.algorithm = value;
    }
    public Integer getDigits() {
        return digits;
    }
    public void setDigits(Integer value) {
        this.digits = value;
    }
    public Integer getPeriodSeconds() {
        return periodSeconds;
    }
    public void setPeriodSeconds(Integer value) {
        this.periodSeconds = value;
    }
    public String getVerifiedAt() {
        return verifiedAt;
    }
    public void setVerifiedAt(String value) {
        this.verifiedAt = value;
    }
    public Long getLastAcceptedStep() {
        return lastAcceptedStep;
    }
    public void setLastAcceptedStep(Long value) {
        this.lastAcceptedStep = value;
    }
}
