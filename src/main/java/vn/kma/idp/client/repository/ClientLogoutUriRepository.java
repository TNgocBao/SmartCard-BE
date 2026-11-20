package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.ClientLogoutUri;
public interface ClientLogoutUriRepository extends JpaRepository<ClientLogoutUri, String> {
}
