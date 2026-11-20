package vn.kma.idp.auth.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.auth.entity.AuthorizationCode;
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, String> {
}
