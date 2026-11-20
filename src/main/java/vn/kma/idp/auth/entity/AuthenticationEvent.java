package vn.kma.idp.auth.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "authentication_event")
public class AuthenticationEvent {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "identity_id", length = 36)
    private String identityId;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "application_id", length = 36)
    private String applicationId;
    @Column(name = "credential_id", length = 36)
    private String credentialId;
    @Column(name = "authentication_method")
    private String authenticationMethod;
    @Column(name = "event_type")
    private String eventType;
    @Column(name = "result")
    private String result;
    @Column(name = "failure_reason_code")
    private String failureReasonCode;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    public AuthenticationEvent() {
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
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String value) {
        this.accountId = value;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String value) {
        this.applicationId = value;
    }
    public String getCredentialId() {
        return credentialId;
    }
    public void setCredentialId(String value) {
        this.credentialId = value;
    }
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }
    public void setAuthenticationMethod(String value) {
        this.authenticationMethod = value;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String value) {
        this.eventType = value;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String value) {
        this.result = value;
    }
    public String getFailureReasonCode() {
        return failureReasonCode;
    }
    public void setFailureReasonCode(String value) {
        this.failureReasonCode = value;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String value) {
        this.ipAddress = value;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String value) {
        this.userAgent = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
}
