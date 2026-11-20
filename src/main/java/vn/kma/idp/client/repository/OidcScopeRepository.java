package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.OidcScope;
public interface OidcScopeRepository extends JpaRepository<OidcScope, String> {
}
