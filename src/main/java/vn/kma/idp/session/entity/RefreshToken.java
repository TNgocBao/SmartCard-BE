package vn.kma.idp.session.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "session_id", length = 36)
    private String sessionId;
    @Column(name = "token_hash")
    private String tokenHash;
    @Column(name = "family_id")
    private String familyId;
    @Column(name = "replaced_by_token_id", length = 36)
    private String replacedByTokenId;
    @Column(name = "status")
    private String status;
    @Column(name = "issued_at", length = 40)
    private String issuedAt;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "consumed_at", length = 40)
    private String consumedAt;
    @Column(name = "revoked_at", length = 40)
    private String revokedAt;
    public RefreshToken() {
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
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String value) {
        this.applicationId = value;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String value) {
        this.sessionId = value;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public void setTokenHash(String value) {
        this.tokenHash = value;
    }
    public String getFamilyId() {
        return familyId;
    }
    public void setFamilyId(String value) {
        this.familyId = value;
    }
    public String getReplacedByTokenId() {
        return replacedByTokenId;
    }
    public void setReplacedByTokenId(String value) {
        this.replacedByTokenId = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getIssuedAt() {
        return issuedAt;
    }
    public void setIssuedAt(String value) {
        this.issuedAt = value;
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
    public String getRevokedAt() {
        return revokedAt;
    }
    public void setRevokedAt(String value) {
        this.revokedAt = value;
    }
}
