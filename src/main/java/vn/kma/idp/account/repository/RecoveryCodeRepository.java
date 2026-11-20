package vn.kma.idp.account.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.account.entity.RecoveryCode;
public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, String> {
}
