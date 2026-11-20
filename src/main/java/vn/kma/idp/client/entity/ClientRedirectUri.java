package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "client_redirect_uri")
public class ClientRedirectUri {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "redirect_uri", columnDefinition = "TEXT")
    private String redirectUri;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public ClientRedirectUri() {
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
    public String getRedirectUri() {
        return redirectUri;
    }
    public void setRedirectUri(String value) {
        this.redirectUri = value;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
