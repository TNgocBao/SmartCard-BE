package vn.kma.idp.account.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "recovery_code")
public class RecoveryCode {
    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(name = "account_id", length = 36)
    private String accountId;
    @Column(name = "code_hash")
    private String codeHash;
    @Column(name = "created_at", length = 40)
    private String createdAt;
    @Column(name = "used_at", length = 40)
    private String usedAt;
    @Column(name = "revoked_at", length = 40)
    private String revokedAt;
    public RecoveryCode() {
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String value) {
        this.accountId = value;
    }
    public String getCodeHash() {
        return codeHash;
    }
    public void setCodeHash(String value) {
        this.codeHash = value;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String value) {
        this.createdAt = value;
    }
    public String getUsedAt() {
        return usedAt;
    }
    public void setUsedAt(String value) {
        this.usedAt = value;
    }
    public String getRevokedAt() {
        return revokedAt;
    }
    public void setRevokedAt(String value) {
        this.revokedAt = value;
    }
}
