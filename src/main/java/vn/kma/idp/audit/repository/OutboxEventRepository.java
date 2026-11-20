package vn.kma.idp.audit.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.audit.entity.OutboxEvent;
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
}
