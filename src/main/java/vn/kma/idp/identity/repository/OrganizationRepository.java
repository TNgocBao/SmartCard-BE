package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.Organization;
public interface OrganizationRepository extends JpaRepository<Organization, String> {
}
