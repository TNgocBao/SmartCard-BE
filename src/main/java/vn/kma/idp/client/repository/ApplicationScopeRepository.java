package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.ApplicationScope;
public interface ApplicationScopeRepository extends JpaRepository<ApplicationScope, String> {
}
