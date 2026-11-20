package vn.kma.idp.rbac.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "account_role_assignment")
public class AccountRoleAssignment {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;
    @Column(name = "role_id", nullable = false, length = 36)
    private String roleId;
    @Column(name = "organization_id", length = 36)
    private String organizationId;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "assigned_by", length = 36)
    private String assignedBy;
    @Column(name = "valid_from", length = 40)
    private String validFrom;
    @Column(name = "valid_until", length = 40)
    private String validUntil;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public AccountRoleAssignment() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String value) {
        this.accountId = value;
    }
    public String getRoleId() {
        return roleId;
    }
    public void setRoleId(String value) {
        this.roleId = value;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String value) {
        this.organizationId = value;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String value) {
        this.applicationId = value;
    }
    public String getAssignedBy() {
        return assignedBy;
    }
    public void setAssignedBy(String value) {
        this.assignedBy = value;
    }
    public String getValidFrom() {
        return validFrom;
    }
    public void setValidFrom(String value) {
        this.validFrom = value;
    }
    public String getValidUntil() {
        return validUntil;
    }
    public void setValidUntil(String value) {
        this.validUntil = value;
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
}
