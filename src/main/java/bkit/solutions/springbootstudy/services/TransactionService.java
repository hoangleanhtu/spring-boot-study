package bkit.solutions.springbootstudy.services;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

  private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
  private static final BigDecimal TRANSFER_FEE = BigDecimal.ONE;
  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  public Collection<TransactionDto> list(final String accountNumber) {
    return transactionRepository.findAllByAccountNumber(accountNumber)
        .stream()
        .map(toDto())
        .toList();
  }

  @Transactional
  public void transferV1(TransferRequest transferPayload) {
    final String sendingAccountNumber = transferPayload.sendingAccountNumber();
    final String receivingAccountNumber = transferPayload.receivingAccountNumber();

    final AccountEntity sendingAccountInfo = accountRepository.findByAccountNumber(sendingAccountNumber).get();
    log.info("Loaded sendingAccountInfo {}", sendingAccountInfo);

    final BigDecimal transactionAmount = transferPayload.amount();
    final BigDecimal currentBalance = sendingAccountInfo.getBalance();

    if (transactionAmount.compareTo(currentBalance) > 0) {
      throw new IllegalArgumentException("Not enough balance");
    }

    sendingAccountInfo.setBalance(currentBalance
        .subtract(transactionAmount));
    log.info("Saving sendingAccountInfo {}", sendingAccountInfo);
    accountRepository.save(sendingAccountInfo);

    final AccountEntity receivingAccountInfo = accountRepository.findByAccountNumber(receivingAccountNumber).get();
    log.info("Loaded receivingAccountInfo {}", receivingAccountInfo);
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
  public void transferFeeV21(TransferRequest transferPayload) {
    final String sendingAccountNumber = transferPayload.sendingAccountNumber();
    final AccountEntity sendingAccountInfo = accountRepository.findByAccountNumber(sendingAccountNumber).get();

    final BigDecimal currentBalance = sendingAccountInfo.getBalance();
    final BigDecimal transferAmount = transferPayload.amount();

    final BigDecimal transferAmountWithFee = currentBalance.compareTo(ONE_THOUSAND) >= 0 ? transferAmount
        : transferAmount.add(
        TRANSFER_FEE);

    if (accountRepository.debit(sendingAccountNumber, transferAmountWithFee) == 0) {
      throw new IllegalArgumentException("Not enough balance");
    }

    final String receivingAccountNumber = transferPayload.receivingAccountNumber();
    transactionRepository.save(
        TransactionEntity.builder()
            .amount(transferAmountWithFee)
            .type(TransactionType.DEBIT)
            .accountNumber(sendingAccountNumber)
            .counterpartyAccountNumber(receivingAccountNumber)
            .build()
    );

    accountRepository.credit(receivingAccountNumber, transferAmount);
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(receivingAccountNumber)
            .amount(transferAmount)
            .counterpartyAccountNumber(sendingAccountNumber)
            .type(TransactionType.CREDIT)
            .build()
    );
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
