package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "identity")
public class Identity {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "person_id", nullable = false, length = 36)
    private String personId;
    @Column(name = "subject_id", nullable = false)
    private String subjectId;
    @Column(name = "identifier")
    private String identifier;
    @Column(name = "identity_type", nullable = false)
    private String identityType;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public Identity() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getPersonId() {
        return personId;
    }
    public void setPersonId(String value) {
        this.personId = value;
    }
    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String value) {
        this.subjectId = value;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String value) {
        this.identifier = value;
    }
    public String getIdentityType() {
        return identityType;
    }
    public void setIdentityType(String value) {
        this.identityType = value;
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
