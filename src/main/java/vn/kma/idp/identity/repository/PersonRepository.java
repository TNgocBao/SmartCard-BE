package vn.kma.idp.identity.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.kma.idp.identity.entity.Person;
public interface PersonRepository extends JpaRepository<Person, String> {
}
