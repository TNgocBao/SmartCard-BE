package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "org_role")
public class OrgRole {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "role_code")
    private String roleCode;
    @Column(name = "role_name")
    private String roleName;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "is_active")
    private Boolean isActive;
    public OrgRole() {
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
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean value) {
        this.isActive = value;
    }
}
