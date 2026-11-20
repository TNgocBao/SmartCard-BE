package vn.kma.idp.session.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.session.entity.IdpSession;
public interface IdpSessionRepository extends JpaRepository<IdpSession, String> {
}
