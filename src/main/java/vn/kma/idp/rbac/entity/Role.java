package vn.kma.idp.rbac.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "role")
public class Role {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "role_code", nullable = false)
    private String roleCode;
    @Column(name = "role_name")
    private String roleName;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "is_system")
    private Boolean isSystem;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public Role() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getRoleCode() {
        return roleCode;
    }
    public void setRoleCode(String value) {
        this.roleCode = value;
    }
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String value) {
        this.roleName = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description = value;
    }
    public Boolean getIsSystem() {
        return isSystem;
    }
    public void setIsSystem(Boolean value) {
        this.isSystem = value;
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
    public String getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(String value) {
        this.updatedAt = value;
    }
}
