package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.TransactionDto;
import bkit.solutions.springbootstudy.dtos.TransactionType;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.dtos.AccountResponse;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.entities.TransactionEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import bkit.solutions.springbootstudy.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public record TransactionService(
    TransactionRepository transactionRepository, AccountRepository accountRepository) {

  public Collection<TransactionDto> list(final String accountNumber) {
    return transactionRepository.findAllByAccountNumber(accountNumber)
        .stream()
        .map(toDto())
        .toList();
  }

  public AccountResponse transfer(TransferRequest transaction) {
    // TODO tu.hoang implement
    throw new IllegalArgumentException("not implement yet");
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

  public AccountResponse deposit(String accountNumber, BigDecimal amount) {
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(accountNumber)
            .type(TransactionType.CREDIT)
            .amount(amount)
            .build()
    );

    final long deposit = accountRepository.deposit(accountNumber, amount);
    log.info("Deposit to account {} {}", accountNumber, deposit == 1);

    final AccountEntity byAccountNumber = accountRepository.findByAccountNumber(accountNumber);

    return new AccountResponse(accountNumber, byAccountNumber.getBalance());
  }
}
