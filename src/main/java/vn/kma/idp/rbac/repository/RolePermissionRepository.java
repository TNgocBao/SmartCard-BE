package vn.kma.idp.rbac.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.rbac.entity.RolePermission;
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
}
