package bkit.solutions.springbootstudy.repositories;

import bkit.solutions.springbootstudy.entities.AccountEntity;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

  AccountEntity findByAccountNumber(String accountNumber);

  @Modifying
  @Query("update AccountEntity b set b.balance = b.balance + :amount where b.accountNumber = :accountNumber")
  long deposit(String accountNumber, BigDecimal amount);
}
