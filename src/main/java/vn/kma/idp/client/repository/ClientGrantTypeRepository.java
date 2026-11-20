package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.ClientGrantType;
public interface ClientGrantTypeRepository extends JpaRepository<ClientGrantType, String> {
}
