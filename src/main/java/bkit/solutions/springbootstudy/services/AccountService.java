package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import java.math.BigDecimal;
import java.util.Collection;
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

  public Collection<AccountResponse> list() {
    return accountRepository.findAll()
        .stream()
        .map(it -> new AccountResponse(it.getAccountNumber(), it.getBalance()))
        .toList();
  }
}
