package vn.kma.idp.credential.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.credential.entity.WebauthnCredential;
public interface WebauthnCredentialRepository extends JpaRepository<WebauthnCredential, String> {
}
