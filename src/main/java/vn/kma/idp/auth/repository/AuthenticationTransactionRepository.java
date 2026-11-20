package vn.kma.idp.auth.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.auth.entity.AuthenticationTransaction;
public interface AuthenticationTransactionRepository extends JpaRepository<AuthenticationTransaction, String> {
}
