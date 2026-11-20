package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "oidc_scope")
public class OidcScope {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "scope_name")
    private String scopeName;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "is_standard")
    private Boolean isStandard;
    @Column(name = "is_active")
    private Boolean isActive;
    public OidcScope() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getScopeName() {
        return scopeName;
    }
    public void setScopeName(String value) {
        this.scopeName = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description = value;
    }
    public Boolean getIsStandard() {
        return isStandard;
    }
    public void setIsStandard(Boolean value) {
        this.isStandard = value;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
}
