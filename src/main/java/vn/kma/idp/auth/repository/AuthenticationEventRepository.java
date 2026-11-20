package vn.kma.idp.auth.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.auth.entity.AuthenticationEvent;
public interface AuthenticationEventRepository extends JpaRepository<AuthenticationEvent, String> {
}
