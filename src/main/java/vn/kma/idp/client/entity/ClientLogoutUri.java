package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "client_logout_uri")
public class ClientLogoutUri {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "post_logout_redirect_uri", columnDefinition = "TEXT")
    private String postLogoutRedirectUri;
    @Column(name = "is_active")
    private Boolean isActive;
    public ClientLogoutUri() {
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
    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }
    public void setPostLogoutRedirectUri(String value) {
        this.postLogoutRedirectUri = value;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
}
