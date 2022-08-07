package bkit.solutions.springbootstudy.repositories;

import bkit.solutions.springbootstudy.entities.UserDemoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDemoRepository extends JpaRepository<UserDemoEntity, Long> {
  Optional<UserDemoEntity> findOneByUsername(String username);
}
