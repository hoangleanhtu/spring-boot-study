package bkit.solutions.springbootstudy.repositories;

import bkit.solutions.springbootstudy.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

}
