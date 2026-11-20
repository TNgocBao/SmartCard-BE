package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.OrgRole;
public interface OrgRoleRepository extends JpaRepository<OrgRole, String> {
}
