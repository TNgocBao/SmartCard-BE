package vn.kma.idp.session.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "user_consent_scope")
public class UserConsentScope {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "user_consent_id", length = 36)
    private String userConsentId;
    @Column(name = "scope_id", length = 36)
    private String scopeId;
    @Column(name = "granted_at", length = 40)
    private String grantedAt;
    public UserConsentScope() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getUserConsentId() {
        return userConsentId;
    }
    public void setUserConsentId(String value) {
        this.userConsentId = value;
    }
    public String getScopeId() {
        return scopeId;
    }
    public void setScopeId(String value) {
        this.scopeId = value;
    }
    public String getGrantedAt() {
        return grantedAt;
    }
    public void setGrantedAt(String value) {
        this.grantedAt = value;
    }
}
