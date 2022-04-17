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
  public AccountResponse transferV1(TransferRequest transaction) {
    final String sendingAccountNumber = transaction.sendingAccountNumber();
    final String receivingAccountNumber = transaction.receivingAccountNumber();

    final List<AccountEntity> accounts = accountRepository.findAllByAccountNumberIn(
        Set.of(sendingAccountNumber, receivingAccountNumber));

    final AccountEntity sendingAccountInfo = getAccountInfo(accounts, sendingAccountNumber,
        "sendingAccountNumber {} does not exist",
        "sendingAccountNumber does not exist");
    log.info("Loaded sendingAccountInfo {}", sendingAccountInfo);

    final AccountEntity receivingAccountInfo = getAccountInfo(accounts, receivingAccountNumber,
        "receivingAccountNumber {} does not exist", "receivingAccountNumber does not exist");
    log.info("Loaded receivingAccountInfo {}", receivingAccountInfo);

    final BigDecimal transactionAmount = transaction.amount();
    final BigDecimal newBalanceOfFromAccount = sendingAccountInfo.getBalance()
        .subtract(transactionAmount);
    if (newBalanceOfFromAccount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Not enough balance");
    }

    sendingAccountInfo.setBalance(newBalanceOfFromAccount);
    log.info("Saving sendingAccountInfo {}", sendingAccountInfo);
    accountRepository.save(sendingAccountInfo);
    transactionRepository.save(
        TransactionEntity.builder()
            .amount(transactionAmount)
            .type(TransactionType.DEBIT)
            .accountNumber(sendingAccountNumber)
            .counterpartyAccountNumber(receivingAccountNumber)
            .build()
    );

    receivingAccountInfo.setBalance(receivingAccountInfo.getBalance().add(transactionAmount));
    log.info("Saving receivingAccountInfo {}", receivingAccountInfo);
    accountRepository.save(receivingAccountInfo);
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(receivingAccountNumber)
            .amount(transactionAmount)
            .counterpartyAccountNumber(sendingAccountNumber)
            .type(TransactionType.CREDIT)
            .build()
    );

    return new AccountResponse(sendingAccountNumber, newBalanceOfFromAccount);
  }

  @Transactional
  public AccountResponse transferV2(TransferRequest transaction) {
    final String sendingAccountNumber = transaction.sendingAccountNumber();
    final String receivingAccountNumber = transaction.receivingAccountNumber();

    final List<AccountEntity> accounts = accountRepository.findAllByAccountNumberIn(
        Set.of(sendingAccountNumber, receivingAccountNumber));

    final AccountEntity fromAccountInfo = getAccountInfo(accounts, sendingAccountNumber,
        "sendingAccountNumber {} does not exist", "sendingAccountNumber does not exist");

    final BigDecimal transactionAmount = transaction.amount();
    final BigDecimal newBalanceOfFromAccount = fromAccountInfo
        .getBalance()
        .subtract(transactionAmount);
    if (accountRepository.debit(sendingAccountNumber, transactionAmount) == 0) {
      throw new IllegalArgumentException("Not enough balance");
    }
    transactionRepository.save(
        TransactionEntity.builder()
            .amount(transactionAmount)
            .type(TransactionType.DEBIT)
            .accountNumber(sendingAccountNumber)
            .counterpartyAccountNumber(receivingAccountNumber)
            .build()
    );

    accountRepository.credit(receivingAccountNumber, transactionAmount);
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(receivingAccountNumber)
            .amount(transactionAmount)
            .counterpartyAccountNumber(sendingAccountNumber)
            .type(TransactionType.CREDIT)
            .build()
    );

    if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("transaction amount must be greater than ZERO");
    }

    return new AccountResponse(sendingAccountNumber, newBalanceOfFromAccount);
  }

  @Transactional
  public AccountResponse deposit(String accountNumber, BigDecimal amount) {

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

  private AccountEntity getAccountInfo(List<AccountEntity> accounts, String fromAccountNumber,
      String s, String errorMessage) {
    return accounts.stream()
        .filter(it -> it.getAccountNumber().equalsIgnoreCase(fromAccountNumber))
        .findFirst()
        .orElseThrow(() -> {
          log.error(s, fromAccountNumber);
          throw new IllegalArgumentException(errorMessage);
        });
  }
}
