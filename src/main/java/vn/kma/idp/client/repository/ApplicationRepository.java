package vn.kma.idp.client.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.client.entity.Application;
public interface ApplicationRepository extends JpaRepository<Application, String> {
}
