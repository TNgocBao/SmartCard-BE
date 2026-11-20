package vn.kma.idp.policy.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "security_policy")
public class SecurityPolicy {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "organization_id", length = 36)
    private String organizationId;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "policy_type")
    private String policyType;
    @Column(name = "policy_name")
    private String policyName;
    @Column(name = "policy_config", columnDefinition = "TEXT")
    private String policyConfig;
    @Column(name = "priority")
    private Integer priority;
    @Column(name = "status")
    private String status;
    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public SecurityPolicy() {
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
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String value) {
        this.applicationId = value;
    }
    public String getPolicyType() {
        return policyType;
    }
    public void setPolicyType(String value) {
        this.policyType = value;
    }
    public String getPolicyName() {
        return policyName;
    }
    public void setPolicyName(String value) {
        this.policyName = value;
    }
    public String getPolicyConfig() {
        return policyConfig;
    }
    public void setPolicyConfig(String value) {
        this.policyConfig = value;
    }
    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer value) {
        this.priority = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getUpdatedBy() {
        return updatedBy;
    }
    public void setUpdatedBy(String value) {
        this.updatedBy = value;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(String value) {
        this.updatedAt = value;
    }
}
