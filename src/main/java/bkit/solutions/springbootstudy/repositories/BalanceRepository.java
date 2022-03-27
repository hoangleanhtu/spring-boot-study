package bkit.solutions.springbootstudy.repositories;

import bkit.solutions.springbootstudy.entities.BalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<BalanceEntity, Long> {

}
