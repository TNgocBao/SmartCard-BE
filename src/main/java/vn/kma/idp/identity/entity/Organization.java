package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "organization")
public class Organization {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "parent_id", length = 36)
    private String parentId;
    @Column(name = "org_code")
    private String orgCode;
    @Column(name = "org_name")
    private String orgName;
    @Column(name = "org_type")
    private String orgType;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "status")
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public Organization() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String value) {
        this.parentId = value;
    }
    public String getOrgCode() {
        return orgCode;
    }
    public void setOrgCode(String value) {
        this.orgCode = value;
    }
    public String getOrgName() {
        return orgName;
    }
    public void setOrgName(String value) {
        this.orgName = value;
    }
    public String getOrgType() {
        return orgType;
    }
    public void setOrgType(String value) {
        this.orgType = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description = value;
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
