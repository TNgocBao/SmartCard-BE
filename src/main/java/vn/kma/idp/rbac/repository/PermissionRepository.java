package vn.kma.idp.rbac.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.rbac.entity.Permission;
public interface PermissionRepository extends JpaRepository<Permission, String> {
}
