package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.TransactionDto;
import bkit.solutions.springbootstudy.repositories.TransactionRepository;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public record TransactionService(
    TransactionRepository transactionRepository) {

  public Collection<TransactionDto> list() {
    return transactionRepository.findAll()
        .stream()
        .map(it -> TransactionDto.builder()
            .accountNumber(it.getAccountNumber())
            .amount(it.getAmount())
            .type(it.getType())
            .counterpartyAccountNumber(it.getCounterpartyAccountNumber())
            .id(it.getId())
            .build()
        )
        .toList();
  }
}
