package vn.kma.idp.credential.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.credential.entity.SmartCardCredential;
public interface SmartCardCredentialRepository extends JpaRepository<SmartCardCredential, String> {
}
