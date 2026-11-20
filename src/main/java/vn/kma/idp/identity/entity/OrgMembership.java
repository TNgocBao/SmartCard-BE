package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "org_membership")
public class OrgMembership {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "identity_id", length = 36)
    private String identityId;
    @Column(name = "organization_id", length = 36)
    private String organizationId;
    @Column(name = "membership_type")
    private String membershipType;
    @Column(name = "status")
    private String status;
    @Column(name = "joined_at")
    private java.time.LocalDate joinedAt;
    @Column(name = "ended_at")
    private java.time.LocalDate endedAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public OrgMembership() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
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
    public String getMembershipType() {
        return membershipType;
    }
    public void setMembershipType(String value) {
        this.membershipType = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public java.time.LocalDate getJoinedAt() {
        return joinedAt;
    }
    public void setJoinedAt(java.time.LocalDate value) {
        this.joinedAt = value;
    }
    public java.time.LocalDate getEndedAt() {
        return endedAt;
    }
    public void setEndedAt(java.time.LocalDate value) {
        this.endedAt = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
