package vn.kma.idp.credential.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "smart_card_credential")
public class SmartCardCredential {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "credential_id", nullable = false, length = 36)
    private String credentialId;
    @Column(name = "card_identifier", nullable = false)
    private String cardIdentifier;
    @Column(name = "public_key_spki", nullable = false, columnDefinition = "TEXT")
    private String publicKeySpki;
    @Column(name = "algorithm", nullable = false)
    private String algorithm;
    @Column(name = "transport", nullable = false)
    private String transport;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public SmartCardCredential() {
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
    public String getCardIdentifier() {
        return cardIdentifier;
    }
    public void setCardIdentifier(String value) {
        this.cardIdentifier = value;
    }
    public String getPublicKeySpki() {
        return publicKeySpki;
    }
    public void setPublicKeySpki(String value) {
        this.publicKeySpki = value;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String value) {
        this.algorithm = value;
    }
    public String getTransport() {
        return transport;
    }
    public void setTransport(String value) {
        this.transport = value;
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
