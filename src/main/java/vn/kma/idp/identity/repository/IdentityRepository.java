package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.Identity;
public interface IdentityRepository extends JpaRepository<Identity, String> {
}
