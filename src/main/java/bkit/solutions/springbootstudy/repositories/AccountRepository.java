package bkit.solutions.springbootstudy.repositories;

import bkit.solutions.springbootstudy.entities.AccountEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

  Optional<AccountEntity> findByAccountNumber(String accountNumber);

  List<AccountEntity> findAllByAccountNumberIn(Set<String> accountNumbers);

  @Modifying
  @Query("update AccountEntity b set b.balance = b.balance + :amount where b.accountNumber = :accountNumber")
  int credit(String accountNumber, BigDecimal amount);

  @Modifying
  @Query("update AccountEntity a set a.balance = a.balance - :amount where a.accountNumber = :accountNumber")
  int debit(String accountNumber, BigDecimal amount);
}