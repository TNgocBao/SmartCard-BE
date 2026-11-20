package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.OrgMembership;
public interface OrgMembershipRepository extends JpaRepository<OrgMembership, String> {
}
