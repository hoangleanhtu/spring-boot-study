package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.dtos.TransactionDto;
import bkit.solutions.springbootstudy.dtos.TransactionType;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.entities.TransactionEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import bkit.solutions.springbootstudy.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  public Collection<TransactionDto> list(final String accountNumber) {
    return transactionRepository.findAllByAccountNumber(accountNumber)
        .stream()
        .map(toDto())
        .toList();
  }

  @Transactional
  public AccountResponse transfer(TransferRequest transaction) {
    final String fromAccountNumber = transaction.fromAccountNumber();
    final String toAccountNumber = transaction.toAccountNumber();

    final List<AccountEntity> accounts = accountRepository.findAllByAccountNumberIn(
        Set.of(fromAccountNumber, toAccountNumber));

    final AccountEntity fromAccountInfo = accounts.stream()
        .filter(it -> it.getAccountNumber().equalsIgnoreCase(fromAccountNumber))
        .findFirst()
        .orElseThrow(() -> {
          log.error("fromAccountNumber {} does not exist", fromAccountNumber);
          throw new IllegalArgumentException("fromAccountNumber does not exist");
        });

    final AccountEntity toAccountInfo = accounts.stream()
        .filter(it -> it.getAccountNumber().equalsIgnoreCase(toAccountNumber))
        .findFirst()
        .orElseThrow(() -> {
          log.error("toAccountNumber {} does not exist", toAccountNumber);
          throw new IllegalArgumentException("toAccountNumber does not exist");
        });

    final BigDecimal transactionAmount = transaction.amount();
    final BigDecimal newBalanceOfFromAccount = fromAccountInfo.getBalance()
        .subtract(transactionAmount);
    if (newBalanceOfFromAccount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Not enough balance");
    }

    fromAccountInfo.setBalance(newBalanceOfFromAccount);
    accountRepository.save(fromAccountInfo);
    transactionRepository.save(
        TransactionEntity.builder()
            .amount(transactionAmount)
            .type(TransactionType.DEBIT)
            .accountNumber(fromAccountNumber)
            .counterpartyAccountNumber(toAccountNumber)
            .build()
    );

    toAccountInfo.setBalance(toAccountInfo.getBalance().add(transactionAmount));
    accountRepository.save(toAccountInfo);
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(toAccountNumber)
            .amount(transactionAmount)
            .counterpartyAccountNumber(fromAccountNumber)
            .type(TransactionType.CREDIT)
            .build()
    );

    return new AccountResponse(fromAccountNumber, newBalanceOfFromAccount);
  }

  @Transactional
  public AccountResponse deposit(String accountNumber, BigDecimal amount) {
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(accountNumber)
            .type(TransactionType.CREDIT)
            .amount(amount)
            .build()
    );

    final int rowUpdated = accountRepository.credit(accountNumber, amount);
    if (rowUpdated == 0) {
      log.error("Account Number {} does not exist", accountNumber);
      throw new IllegalArgumentException("Account Number does not exist");
    }

    final AccountEntity byAccountNumber = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> {
          log.error("Account Number {} does not exist", accountNumber);
          throw new IllegalArgumentException("Account Number does not exist");
        });

    return new AccountResponse(accountNumber, byAccountNumber.getBalance());
  }

  private Function<TransactionEntity, TransactionDto> toDto() {
    return it -> TransactionDto.builder()
        .accountNumber(it.getAccountNumber())
        .amount(it.getAmount())
        .type(it.getType())
        .counterpartyAccountNumber(it.getCounterpartyAccountNumber())
        .id(it.getId())
        .build();
  }
}
