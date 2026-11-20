package vn.kma.idp.account.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.account.entity.Account;
public interface AccountEntityRepository extends JpaRepository<Account,String> {
}
