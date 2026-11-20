package vn.kma.idp.credential.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.credential.entity.Credential;
public interface CredentialRepository extends JpaRepository<Credential, String> {
}
