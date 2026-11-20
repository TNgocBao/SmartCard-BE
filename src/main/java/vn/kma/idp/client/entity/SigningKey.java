package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "signing_key")
public class SigningKey {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "kid")
    private String kid;
    @Column(name = "algorithm")
    private String algorithm;
    @Column(name = "public_jwk", columnDefinition = "TEXT")
    private String publicJwk;
    @Column(name = "private_key_ref", columnDefinition = "TEXT")
    private String privateKeyRef;
    @Column(name = "status")
    private String status;
    @Column(name = "not_before", length = 40)
    private String notBefore;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name="active_slot")
    private Integer activeSlot;
    public Integer getActiveSlot(){return activeSlot;}
    public void setActiveSlot(Integer value){activeSlot=value;}
    public SigningKey() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getKid() {
        return kid;
    }
    public void setKid(String value) {
        this.kid = value;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String value) {
        this.algorithm = value;
    }
    public String getPublicJwk() {
        return publicJwk;
    }
    public void setPublicJwk(String value) {
        this.publicJwk = value;
    }
    public String getPrivateKeyRef() {
        return privateKeyRef;
    }
    public void setPrivateKeyRef(String value) {
        this.privateKeyRef = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getNotBefore() {
        return notBefore;
    }
    public void setNotBefore(String value) {
        this.notBefore = value;
    }
    public String getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(String value) {
        this.expiresAt = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
