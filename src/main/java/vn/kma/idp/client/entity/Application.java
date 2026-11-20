package vn.kma.idp.client.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "application")
public class Application {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "organization_id", length = 36)
    private String organizationId;
    @Column(name = "client_id")
    private String clientId;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "client_type")
    private String clientType;
    @Column(name = "protocol")
    private String protocol;
    @Column(name = "client_auth_method")
    private String clientAuthMethod;
    @Column(name = "require_pkce")
    private Boolean requirePkce;
    @Column(name = "status")
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public Application() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String value) {
        this.organizationId = value;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String value) {
        this.clientId = value;
    }
    public String getClientName() {
        return clientName;
    }
    public void setClientName(String value) {
        this.clientName = value;
    }
    public String getClientType() {
        return clientType;
    }
    public void setClientType(String value) {
        this.clientType = value;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String value) {
        this.protocol = value;
    }
    public String getClientAuthMethod() {
        return clientAuthMethod;
    }
    public void setClientAuthMethod(String value) {
        this.clientAuthMethod = value;
    }
    public Boolean getRequirePkce() {
        return requirePkce;
    }
    public void setRequirePkce(Boolean value) {
        this.requirePkce = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
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
