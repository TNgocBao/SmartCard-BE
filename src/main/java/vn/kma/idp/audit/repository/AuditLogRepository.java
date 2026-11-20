package vn.kma.idp.audit.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.audit.entity.AuditLog;
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
