package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "client_grant_type")
public class ClientGrantType {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "grant_type")
    private String grantType;
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    public ClientGrantType() {
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
    public String getGrantType() {
        return grantType;
    }
    public void setGrantType(String value) {
        this.grantType = value;
    }
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(Boolean value) {
        this.isEnabled = value;
    }
}
