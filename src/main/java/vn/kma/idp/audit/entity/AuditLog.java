package vn.kma.idp.audit.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "actor_account_id", length = 36)
    private String actorAccountId;
    @Column(name = "event_type")
    private String eventType;
    @Column(name = "action")
    private String action;
    @Column(name = "resource_type")
    private String resourceType;
    @Column(name = "resource_id")
    private String resourceId;
    @Column(name = "result")
    private String result;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "correlation_id")
    private String correlationId;
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public AuditLog() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getActorAccountId() {
        return actorAccountId;
    }
    public void setActorAccountId(String value) {
        this.actorAccountId = value;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String value) {
        this.eventType = value;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String value) {
        this.action = value;
    }
    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String value) {
        this.resourceType = value;
    }
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String value) {
        this.resourceId = value;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String value) {
        this.result = value;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String value) {
        this.ipAddress = value;
    }
    public String getCorrelationId() {
        return correlationId;
    }
    public void setCorrelationId(String value) {
        this.correlationId = value;
    }
    public String getMetadata() {
        return metadata;
    }
    public void setMetadata(String value) {
        this.metadata = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
