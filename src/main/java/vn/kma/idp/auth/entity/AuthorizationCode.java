package vn.kma.idp.auth.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "authorization_code")
public class AuthorizationCode {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "session_id", length = 36)
    private String sessionId;
    @Column(name = "code_hash")
    private String codeHash;
    @Column(name = "redirect_uri", columnDefinition = "TEXT")
    private String redirectUri;
    @Column(name = "nonce")
    private String nonce;
    @Column(name = "code_challenge")
    private String codeChallenge;
    @Column(name = "code_challenge_method")
    private String codeChallengeMethod;
    @Column(name = "expires_at", length = 40)
    private String expiresAt;
    @Column(name = "consumed_at", length = 40)
    private String consumedAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public AuthorizationCode() {
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
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String value) {
        this.accountId = value;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String value) {
        this.sessionId = value;
    }
    public String getCodeHash() {
        return codeHash;
    }
    public void setCodeHash(String value) {
        this.codeHash = value;
    }
    public String getRedirectUri() {
        return redirectUri;
    }
    public void setRedirectUri(String value) {
        this.redirectUri = value;
    }
    public String getNonce() {
        return nonce;
    }
    public void setNonce(String value) {
        this.nonce = value;
    }
    public String getCodeChallenge() {
        return codeChallenge;
    }
    public void setCodeChallenge(String value) {
        this.codeChallenge = value;
    }
    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }
    public void setCodeChallengeMethod(String value) {
        this.codeChallengeMethod = value;
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
