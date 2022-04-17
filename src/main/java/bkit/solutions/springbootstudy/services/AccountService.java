package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;

  public AccountResponse create(String accountNumber, BigDecimal depositAmount) {

    final AccountEntity createdAccount = accountRepository.save(
        AccountEntity.builder()
            .accountNumber(accountNumber)
            .balance(depositAmount)
            .build());

    return new AccountResponse(createdAccount.getAccountNumber(), createdAccount.getBalance());
  }

}
