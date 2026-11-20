package vn.kma.idp.credential.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.credential.entity.WebauthnChallenge;
public interface WebauthnChallengeRepository extends JpaRepository<WebauthnChallenge, String> {
}
