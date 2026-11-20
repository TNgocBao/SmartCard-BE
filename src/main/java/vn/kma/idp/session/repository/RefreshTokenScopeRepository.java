package vn.kma.idp.session.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.session.entity.RefreshTokenScope;
public interface RefreshTokenScopeRepository extends JpaRepository<RefreshTokenScope, String> {
}
