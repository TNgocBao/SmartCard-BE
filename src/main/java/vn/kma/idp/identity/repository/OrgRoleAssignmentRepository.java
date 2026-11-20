package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.OrgRoleAssignment;
public interface OrgRoleAssignmentRepository extends JpaRepository<OrgRoleAssignment, String> {
}
