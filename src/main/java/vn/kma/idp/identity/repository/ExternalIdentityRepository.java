package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.ExternalIdentity;
public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity, String> {
}
