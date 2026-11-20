package vn.kma.idp.credential.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "credential")
public class Credential {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;
    @Column(name = "credential_type", nullable = false)
    private String credentialType;
    @Column(name = "credential_name")
    private String credentialName;
    @Column(name = "credential_identifier")
    private String credentialIdentifier;
    @Column(name = "secret_hash", columnDefinition = "TEXT")
    private String secretHash;
    @Column(name = "secret_reference", columnDefinition = "TEXT")
    private String secretReference;
    @Column(name = "certificate_fingerprint")
    private String certificateFingerprint;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "registered_at", length = 40)
    private String registeredAt;
    @Column(name = "last_used_at", length = 40)
    private String lastUsedAt;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "revoked_at", length = 40)
    private String revokedAt;
    public Credential() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String value) {
        this.accountId = value;
    }
    public String getCredentialType() {
        return credentialType;
    }
    public void setCredentialType(String value) {
        this.credentialType = value;
    }
    public String getCredentialName() {
        return credentialName;
    }
    public void setCredentialName(String value) {
        this.credentialName = value;
    }
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }
    public void setCredentialIdentifier(String value) {
        this.credentialIdentifier = value;
    }
    public String getSecretHash() {
        return secretHash;
    }
    public void setSecretHash(String value) {
        this.secretHash = value;
    }
    public String getSecretReference() {
        return secretReference;
    }
    public void setSecretReference(String value) {
        this.secretReference = value;
    }
    public String getCertificateFingerprint() {
        return certificateFingerprint;
    }
    public void setCertificateFingerprint(String value) {
        this.certificateFingerprint = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getRegisteredAt() {
        return registeredAt;
    }
    public void setRegisteredAt(String value) {
        this.registeredAt = value;
    }
    public String getLastUsedAt() {
        return lastUsedAt;
    }
    public void setLastUsedAt(String value) {
        this.lastUsedAt = value;
    }
    public String getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(String value) {
        this.expiresAt = value;
    }
    public String getRevokedAt() {
        return revokedAt;
    }
    public void setRevokedAt(String value) {
        this.revokedAt = value;
    }
}
