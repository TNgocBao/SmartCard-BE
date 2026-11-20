package vn.kma.idp.credential.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.credential.entity.SmartCardChallenge;
public interface SmartCardChallengeRepository extends JpaRepository<SmartCardChallenge, String> {
}
