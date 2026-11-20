package vn.kma.idp.account.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.account.entity.AccountActivationToken;
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, String> {
}
