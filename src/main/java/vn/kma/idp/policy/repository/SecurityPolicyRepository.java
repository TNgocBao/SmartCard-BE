package vn.kma.idp.policy.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.policy.entity.SecurityPolicy;
public interface SecurityPolicyRepository extends JpaRepository<SecurityPolicy, String> {
}
