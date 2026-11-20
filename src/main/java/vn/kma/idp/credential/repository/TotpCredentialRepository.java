package vn.kma.idp.credential.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.credential.entity.TotpCredential;
public interface TotpCredentialRepository extends JpaRepository<TotpCredential, String> {
}
