package vn.kma.idp.rbac.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "role_permission")
public class RolePermission {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;
    @Column(name = "permission_id", nullable = false, length = 36)
    private String permissionId;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public RolePermission() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getRoleId() {
        return roleId;
    }
    public void setRoleId(String value) {
        this.roleId = value;
    }
    public String getPermissionId() {
        return permissionId;
    }
    public void setPermissionId(String value) {
        this.permissionId = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
