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
  public void transferV1(TransferRequest transaction) {
    final String sendingAccountNumber = transaction.sendingAccountNumber();
    final String receivingAccountNumber = transaction.receivingAccountNumber();

    final AccountEntity sendingAccountInfo = accountRepository.findByAccountNumber(sendingAccountNumber).get();
    log.info("Loaded sendingAccountInfo {}", sendingAccountInfo);

    final AccountEntity receivingAccountInfo = accountRepository.findByAccountNumber(receivingAccountNumber).get();
    log.info("Loaded receivingAccountInfo {}", receivingAccountInfo);

    final BigDecimal transactionAmount = transaction.amount();
    final BigDecimal currentBalance = sendingAccountInfo.getBalance();

    if (transactionAmount.compareTo(currentBalance) > 0) {
      throw new IllegalArgumentException("Not enough balance");
    }

    sendingAccountInfo.setBalance(currentBalance
        .subtract(transactionAmount));
    log.info("Saving sendingAccountInfo {}", sendingAccountInfo);
    accountRepository.save(sendingAccountInfo);

    receivingAccountInfo.setBalance(receivingAccountInfo.getBalance().add(transactionAmount));
    log.info("Saving receivingAccountInfo {}", receivingAccountInfo);
    accountRepository.save(receivingAccountInfo);

    transactionRepository.save(
        TransactionEntity.builder()
            .amount(transactionAmount)
            .type(TransactionType.DEBIT)
            .accountNumber(sendingAccountNumber)
            .counterpartyAccountNumber(receivingAccountNumber)
            .build()
    );

    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(receivingAccountNumber)
            .amount(transactionAmount)
            .counterpartyAccountNumber(sendingAccountNumber)
            .type(TransactionType.CREDIT)
            .build()
    );
  }

  @Transactional
  public void transferV2(TransferRequest transaction) {
    final String sendingAccountNumber = transaction.sendingAccountNumber();
    final BigDecimal transactionAmount = transaction.amount();

    if (accountRepository.debit(sendingAccountNumber, transactionAmount) == 0) {
      throw new IllegalArgumentException("Not enough balance");
    }

    final String receivingAccountNumber = transaction.receivingAccountNumber();
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
}
