package vn.kma.idp.session.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.session.entity.UserConsentScope;
public interface UserConsentScopeRepository extends JpaRepository<UserConsentScope, String> {
}
