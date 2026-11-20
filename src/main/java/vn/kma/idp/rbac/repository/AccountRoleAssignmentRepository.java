package vn.kma.idp.rbac.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.rbac.entity.AccountRoleAssignment;
public interface AccountRoleAssignmentRepository extends JpaRepository<AccountRoleAssignment, String> {
}
