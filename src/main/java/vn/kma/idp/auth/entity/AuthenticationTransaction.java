package vn.kma.idp.auth.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "authentication_transaction")
public class AuthenticationTransaction {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "required_acr")
    private String requiredAcr;
    @Column(name = "completed_amr", columnDefinition = "TEXT")
    private String completedAmr;
    @Column(name = "oidc_context_json", columnDefinition = "TEXT")
    private String oidcContextJson;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "transaction_token_hash", length = 64)
    private String tokenHash;
    @Column(name = "failed_attempts")
    private Integer failedAttempts;
    @Column(name = "consumed_at", length = 40)
    private String consumedAt;
    public String getTokenHash(){return tokenHash;}
    public void setTokenHash(String value){tokenHash=value;}
    public Integer getFailedAttempts(){return failedAttempts;}
    public void setFailedAttempts(Integer value){failedAttempts=value;}
    public String getConsumedAt(){return consumedAt;}
    public void setConsumedAt(String value){consumedAt=value;}
    public AuthenticationTransaction() {
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
    public String getRequiredAcr() {
        return requiredAcr;
    }
    public void setRequiredAcr(String value) {
        this.requiredAcr = value;
    }
    public String getCompletedAmr() {
        return completedAmr;
    }
    public void setCompletedAmr(String value) {
        this.completedAmr = value;
    }
    public String getOidcContextJson() {
        return oidcContextJson;
    }
    public void setOidcContextJson(String value) {
        this.oidcContextJson = value;
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
