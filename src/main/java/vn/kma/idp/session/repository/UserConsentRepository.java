package vn.kma.idp.session.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.session.entity.UserConsent;
public interface UserConsentRepository extends JpaRepository<UserConsent, String> {
}
