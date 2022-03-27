package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public record AccountService(AccountRepository accountRepository) {

  public AccountResponse create(String accountNumber) {

    final AccountEntity createdAccount = accountRepository.save(
        AccountEntity.builder()
            .accountNumber(accountNumber)
            .balance(BigDecimal.ZERO)
            .build());

    return new AccountResponse(createdAccount.getAccountNumber(), createdAccount.getBalance());
  }
}
