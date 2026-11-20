package vn.kma.idp.session.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "idp_session")
public class IdpSession {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;
    @Column(name = "session_id_hash", nullable = false)
    private String sessionIdHash;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "authentication_level")
    private String authenticationLevel;
    @Column(name = "amr", columnDefinition = "TEXT")
    private String amr;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    @Column(name = "authenticated_at", length = 40)
    private String authenticatedAt;
    @Column(name = "last_activity_at", length = 40)
    private String lastActivityAt;
    @Column(name = "expires_at", nullable = false, length = 40)
    private String expiresAt;
    @Column(name = "revoked_at", length = 40)
    private String revokedAt;
    public IdpSession() {
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
    public String getSessionIdHash() {
        return sessionIdHash;
    }
    public void setSessionIdHash(String value) {
        this.sessionIdHash = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getAuthenticationLevel() {
        return authenticationLevel;
    }
    public void setAuthenticationLevel(String value) {
        this.authenticationLevel = value;
    }
    public String getAmr() {
        return amr;
    }
    public void setAmr(String value) {
        this.amr = value;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String value) {
        this.ipAddress = value;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String value) {
        this.userAgent = value;
    }
    public String getAuthenticatedAt() {
        return authenticatedAt;
    }
    public void setAuthenticatedAt(String value) {
        this.authenticatedAt = value;
    }
    public String getLastActivityAt() {
        return lastActivityAt;
    }
    public void setLastActivityAt(String value) {
        this.lastActivityAt = value;
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
