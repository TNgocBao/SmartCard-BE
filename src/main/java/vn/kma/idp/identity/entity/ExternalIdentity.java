package vn.kma.idp.identity.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "external_identity")
public class ExternalIdentity {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "identity_id", length = 36)
    private String identityId;
    @Column(name = "issuer")
    private String issuer;
    @Column(name = "external_subject")
    private String externalSubject;
    @Column(name = "provider")
    private String provider;
    @Column(name = "linked_at", length = 40)
    private String linkedAt;
    public ExternalIdentity() {
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
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String value) {
        this.issuer = value;
    }
    public String getExternalSubject() {
        return externalSubject;
    }
    public void setExternalSubject(String value) {
        this.externalSubject = value;
    }
    public String getProvider() {
        return provider;
    }
    public void setProvider(String value) {
        this.provider = value;
    }
    public String getLinkedAt() {
        return linkedAt;
    }
    public void setLinkedAt(String value) {
        this.linkedAt = value;
    }
}
