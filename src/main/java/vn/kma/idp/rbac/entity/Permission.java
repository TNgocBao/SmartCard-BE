package vn.kma.idp.rbac.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "permission_code", nullable = false)
    private String permissionCode;
    @Column(name = "resource")
    private String resource;
    @Column(name = "action")
    private String action;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "is_active")
    private Boolean isActive;
    public Permission() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getPermissionCode() {
        return permissionCode;
    }
    public void setPermissionCode(String value) {
        this.permissionCode = value;
    }
    public String getResource() {
        return resource;
    }
    public void setResource(String value) {
        this.resource = value;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String value) {
        this.action = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description = value;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
}
