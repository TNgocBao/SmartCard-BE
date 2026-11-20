package vn.kma.idp.credential.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "webauthn_challenge")
public class WebauthnChallenge {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "challenge")
    private String challenge;
    @Column(name = "ceremony_type")
    private String ceremonyType;
    @Column(name = "rp_id")
    private String rpId;
    @Column(name = "user_verification")
    private String userVerification;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "consumed_at", length = 40)
    private String consumedAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public WebauthnChallenge() {
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
    public String getChallenge() {
        return challenge;
    }
    public void setChallenge(String value) {
        this.challenge = value;
    }
    public String getCeremonyType() {
        return ceremonyType;
    }
    public void setCeremonyType(String value) {
        this.ceremonyType = value;
    }
    public String getRpId() {
        return rpId;
    }
    public void setRpId(String value) {
        this.rpId = value;
    }
    public String getUserVerification() {
        return userVerification;
    }
    public void setUserVerification(String value) {
        this.userVerification = value;
    }
    public String getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(String value) {
        this.expiresAt = value;
    }
    public String getConsumedAt() {
        return consumedAt;
    }
    public void setConsumedAt(String value) {
        this.consumedAt = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
