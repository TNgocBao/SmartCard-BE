package vn.kma.idp.rbac.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.rbac.entity.Role;
public interface RoleRepository extends JpaRepository<Role, String> {
}
