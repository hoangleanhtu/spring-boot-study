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

  @Transactional
  public AccountResponse deposit(String accountNumber, BigDecimal amount) {
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(accountNumber)
            .type(TransactionType.CREDIT)
            .amount(amount)
            .build()
    );

    final int rowUpdated = accountRepository.deposit(accountNumber, amount);
    if (rowUpdated == 0) {
      log.error("Account Number {} does not exist", accountNumber);
      throw new IllegalArgumentException("Account Number does not exist");
    }

    final AccountEntity byAccountNumber = accountRepository.findByAccountNumber(accountNumber);

    return new AccountResponse(accountNumber, byAccountNumber.getBalance());
  }
}
