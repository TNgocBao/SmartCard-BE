package vn.kma.idp.account.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "account")
public class Account {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "identity_id", nullable = false, length = 36)
    private String identityId;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "account_type")
    private String accountType;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;
    @Column(name = "locked_until", length = 40)
    private String lockedUntil;
    @Column(name = "lock_reason")
    private String lockReason;
    @Column(name = "webauthn_user_handle", columnDefinition = "TEXT")
    private String webauthnUserHandle;
    @Column(name = "last_login_at", length = 40)
    private String lastLoginAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public Account() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getIdentityId() {
        return identityId;
    }
    public void setIdentityId(String value) {
        this.identityId = value;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String value) {
        this.username = value;
    }
    public String getAccountType() {
        return accountType;
    }
    public void setAccountType(String value) {
        this.accountType = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public Boolean getIsLocked() {
        return isLocked;
    }
    public void setIsLocked(Boolean value) {
        this.isLocked = value;
    }
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    public void setFailedLoginAttempts(Integer value) {
        this.failedLoginAttempts = value;
    }
    public String getLockedUntil() {
        return lockedUntil;
    }
    public void setLockedUntil(String value) {
        this.lockedUntil = value;
    }
    public String getLockReason() {
        return lockReason;
    }
    public void setLockReason(String value) {
        this.lockReason = value;
    }
    public String getWebauthnUserHandle() {
        return webauthnUserHandle;
    }
    public void setWebauthnUserHandle(String value) {
        this.webauthnUserHandle = value;
    }
    public String getLastLoginAt() {
        return lastLoginAt;
    }
    public void setLastLoginAt(String value) {
        this.lastLoginAt = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(String value) {
        this.updatedAt = value;
    }
}
