package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "client_credential")
public class ClientCredential {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "credential_type")
    private String credentialType;
    @Column(name = "credential_name")
    private String credentialName;
    @Column(name = "secret_hash", columnDefinition = "TEXT")
    private String secretHash;
    @Column(name = "secret_reference", columnDefinition = "TEXT")
    private String secretReference;
    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;
    @Column(name = "certificate_fingerprint")
    private String certificateFingerprint;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "revoked_at", length = 40)
    private String revokedAt;
    @Column(name = "status")
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public ClientCredential() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String value) {
        this.applicationId = value;
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
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String value) {
        this.publicKey = value;
    }
    public String getCertificateFingerprint() {
        return certificateFingerprint;
    }
    public void setCertificateFingerprint(String value) {
        this.certificateFingerprint = value;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
