package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "org_role_assignment")
public class OrgRoleAssignment {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "membership_id", length = 36)
    private String membershipId;
    @Column(name = "identity_id", length = 36)
    private String identityId;
    @Column(name = "organization_id", length = 36)
    private String organizationId;
    @Column(name = "org_role_id", length = 36)
    private String orgRoleId;
    @Column(name = "assigned_by", length = 36)
    private String assignedBy;
    @Column(name = "valid_from", length = 40)
    private String validFrom;
    @Column(name = "valid_until", length = 40)
    private String validUntil;
    @Column(name = "status")
    private String status;
    public OrgRoleAssignment() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getMembershipId() {
        return membershipId;
    }
    public void setMembershipId(String value) {
        this.membershipId = value;
    }
    public String getIdentityId() {
        return identityId;
    }
    public void setIdentityId(String value) {
        this.identityId = value;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String value) {
        this.organizationId = value;
    }
    public String getOrgRoleId() {
        return orgRoleId;
    }
    public void setOrgRoleId(String value) {
        this.orgRoleId = value;
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
}
