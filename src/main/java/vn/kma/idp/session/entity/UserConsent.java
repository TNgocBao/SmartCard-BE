package vn.kma.idp.session.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "user_consent")
public class UserConsent {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "identity_id", length = 36)
    private String identityId;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "status")
    private String status;
    @Column(name = "granted_at", length = 40)
    private String grantedAt;
    @Column(name = "revoked_at", length = 40)
    private String revokedAt;
    public UserConsent() {
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getGrantedAt() {
        return grantedAt;
    }
    public void setGrantedAt(String value) {
        this.grantedAt = value;
    }
    public String getRevokedAt() {
        return revokedAt;
    }
    public void setRevokedAt(String value) {
        this.revokedAt = value;
    }
}
