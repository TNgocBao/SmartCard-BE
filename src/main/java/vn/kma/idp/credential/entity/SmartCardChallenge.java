package vn.kma.idp.credential.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "smart_card_challenge")
public class SmartCardChallenge {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;
    @Column(name = "credential_id", nullable = false, length = 36)
    private String credentialId;
    @Column(name = "session_id", length = 36)
    private String sessionId;
    @Column(name = "challenge_hash", nullable = false)
    private String challengeHash;
    @Column(name = "challenge_payload", nullable = false, columnDefinition = "TEXT")
    private String challengePayload;
    @Column(name = "expires_at", nullable = false, length = 40)
    private String expiresAt;
    @Column(name = "consumed_at", length = 40)
    private String consumedAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public SmartCardChallenge() {
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
    public String getCredentialId() {
        return credentialId;
    }
    public void setCredentialId(String value) {
        this.credentialId = value;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String value) {
        this.sessionId = value;
    }
    public String getChallengeHash() {
        return challengeHash;
    }
    public void setChallengeHash(String value) {
        this.challengeHash = value;
    }
    public String getChallengePayload() {
        return challengePayload;
    }
    public void setChallengePayload(String value) {
        this.challengePayload = value;
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
