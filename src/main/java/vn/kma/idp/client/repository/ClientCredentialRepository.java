package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.ClientCredential;
public interface ClientCredentialRepository extends JpaRepository<ClientCredential, String> {
}
