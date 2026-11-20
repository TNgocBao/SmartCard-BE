package vn.kma.idp.account.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "account_activation_token")
public class AccountActivationToken {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "token_hash")
    private String tokenHash;
    @Column(name = "purpose")
    private String purpose;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "consumed_at", length = 40)
    private String consumedAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public AccountActivationToken() {
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
    public String getTokenHash() {
        return tokenHash;
    }
    public void setTokenHash(String value) {
        this.tokenHash = value;
    }
    public String getPurpose() {
        return purpose;
    }
    public void setPurpose(String value) {
        this.purpose = value;
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
