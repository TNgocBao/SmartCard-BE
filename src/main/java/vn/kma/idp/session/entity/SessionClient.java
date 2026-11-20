package vn.kma.idp.session.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "session_client")
public class SessionClient {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "session_id", length = 36)
    private String sessionId;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "joined_at", length = 40)
    private String joinedAt;
    @Column(name = "logged_out_at", length = 40)
    private String loggedOutAt;
    public SessionClient() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String value) {
        this.sessionId = value;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String value) {
        this.applicationId = value;
    }
    public String getJoinedAt() {
        return joinedAt;
    }
    public void setJoinedAt(String value) {
        this.joinedAt = value;
    }
    public String getLoggedOutAt() {
        return loggedOutAt;
    }
    public void setLoggedOutAt(String value) {
        this.loggedOutAt = value;
    }
}
