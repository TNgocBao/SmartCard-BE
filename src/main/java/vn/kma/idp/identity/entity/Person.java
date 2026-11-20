package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "person")
public class Person {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "person_code")
    private String personCode;
    @Column(name = "display_name", nullable = false)
    private String displayName;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "updated_at", length = 40)
    private String updatedAt;
    public Person() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getPersonCode() {
        return personCode;
    }
    public void setPersonCode(String value) {
        this.personCode = value;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String value) {
        this.displayName = value;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String value) {
        this.email = value;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String value) {
        this.phone = value;
    }
    public java.time.LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(java.time.LocalDate value) {
        this.dateOfBirth = value;
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
