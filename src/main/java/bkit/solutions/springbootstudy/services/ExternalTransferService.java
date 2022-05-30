package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.clients.ExternalBankClient;
import bkit.solutions.springbootstudy.clients.dtos.PostExternalTransferRequest;
import bkit.solutions.springbootstudy.clients.dtos.PostExternalTransferResponse;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.entities.TransactionEntity;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferException;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import bkit.solutions.springbootstudy.repositories.TransactionRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferService {
  private final ExternalBankClient externalBankClient;
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  @Transactional
  public AccountEntity transfer(TransferRequest transferRequest) throws ExternalTransferException {
    final String sendingAccountNumber = transferRequest.sendingAccountNumber();
    final AccountEntity sendingAccount = accountRepository.findByAccountNumber(
        sendingAccountNumber).get();

    final BigDecimal amount = transferRequest.amount();
    final BigDecimal currentBalance = sendingAccount.getBalance();
    if (currentBalance.compareTo(amount) < 0) {
      throw ExternalTransferException.NOT_ENOUGH_BALANCE;
    }

    final String receivingAccountNumber = transferRequest.receivingAccountNumber();
    final PostExternalTransferResponse transferResponse = externalBankClient.transfer(
        PostExternalTransferRequest.builder()
            .amount(amount)
            .fromAccountNumber(sendingAccountNumber)
            .toAccountNumber(receivingAccountNumber)
            .build()
    );

    switch (transferResponse.getErrorCode()) {
      case ExternalTransferException.RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE -> throw ExternalTransferException.RECEIVING_ACCOUNT_NOT_FOUND;
      case ExternalTransferException.RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE -> throw ExternalTransferException.RECEIVING_ACCOUNT_INACTIVE;
    }

    sendingAccount.setBalance(currentBalance.subtract(amount));
    accountRepository.save(sendingAccount);
    transactionRepository.save(
        TransactionEntity.builder()
            .accountNumber(sendingAccountNumber)
            .amount(amount)
            .counterpartyAccountNumber(receivingAccountNumber)
            .build()
    );

    return sendingAccount;
  }

}
