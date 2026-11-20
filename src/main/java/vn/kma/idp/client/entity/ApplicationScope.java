package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "application_scope")
public class ApplicationScope {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "scope_id", length = 36)
    private String scopeId;
    @Column(name = "is_default")
    private Boolean isDefault;
    @Column(name = "is_required")
    private Boolean isRequired;
    @Column(name = "status")
    private String status;
    public ApplicationScope() {
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
    public String getScopeId() {
        return scopeId;
    }
    public void setScopeId(String value) {
        this.scopeId = value;
    }
    public Boolean getIsDefault() {
        return isDefault;
    }
    public void setIsDefault(Boolean value) {
        this.isDefault = value;
    }
    public Boolean getIsRequired() {
        return isRequired;
    }
    public void setIsRequired(Boolean value) {
        this.isRequired = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
}
