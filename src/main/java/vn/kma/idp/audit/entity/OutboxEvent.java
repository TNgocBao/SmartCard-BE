package vn.kma.idp.audit.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "event_type")
    private String eventType;
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
    @Column(name = "status")
    private String status;
    @Column(name = "attempts")
    private Integer attempts;
    @Column(name = "next_attempt_at", length = 40)
    private String nextAttemptAt;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public OutboxEvent() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String value) {
        this.eventType = value;
    }
    public String getPayload() {
        return payload;
    }
    public void setPayload(String value) {
        this.payload = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public Integer getAttempts() {
        return attempts;
    }
    public void setAttempts(Integer value) {
        this.attempts = value;
    }
    public String getNextAttemptAt() {
        return nextAttemptAt;
    }
    public void setNextAttemptAt(String value) {
        this.nextAttemptAt = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
