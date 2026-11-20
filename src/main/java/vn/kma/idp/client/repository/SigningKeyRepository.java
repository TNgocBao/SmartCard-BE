package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.SigningKey;
public interface SigningKeyRepository extends JpaRepository<SigningKey, String> {
}
