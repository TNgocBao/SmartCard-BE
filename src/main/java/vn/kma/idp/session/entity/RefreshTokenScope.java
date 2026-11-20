package vn.kma.idp.session.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "refresh_token_scope")
public class RefreshTokenScope {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "refresh_token_id", length = 36)
    private String refreshTokenId;
    @Column(name = "scope_id", length = 36)
    private String scopeId;
    public RefreshTokenScope() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getRefreshTokenId() {
        return refreshTokenId;
    }
    public void setRefreshTokenId(String value) {
        this.refreshTokenId = value;
    }
    public String getScopeId() {
        return scopeId;
    }
    public void setScopeId(String value) {
        this.scopeId = value;
    }
}
