package vn.kma.idp.session.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.session.entity.SessionClient;
public interface SessionClientRepository extends JpaRepository<SessionClient, String> {
}
