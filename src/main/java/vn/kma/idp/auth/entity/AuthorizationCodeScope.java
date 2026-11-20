package vn.kma.idp.auth.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "authorization_code_scope")
public class AuthorizationCodeScope {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "authorization_code_id", length = 36)
    private String authorizationCodeId;
    @Column(name = "scope_id", length = 36)
    private String scopeId;
    public AuthorizationCodeScope() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getAuthorizationCodeId() {
        return authorizationCodeId;
    }
    public void setAuthorizationCodeId(String value) {
        this.authorizationCodeId = value;
    }
    public String getScopeId() {
        return scopeId;
    }
    public void setScopeId(String value) {
        this.scopeId = value;
    }
}
