package bkit.solutions.springbootstudy.services;

import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE;

import bkit.solutions.springbootstudy.clients.ExternalBankClient;
import bkit.solutions.springbootstudy.clients.dtos.PostExternalTransferRequest;
import bkit.solutions.springbootstudy.clients.dtos.PostExternalTransferResponse;
import bkit.solutions.springbootstudy.config.AmqpProperties;
import bkit.solutions.springbootstudy.dtos.BalanceChangeDto;
import bkit.solutions.springbootstudy.dtos.TransactionType;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.entities.TransactionEntity;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferException;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import bkit.solutions.springbootstudy.repositories.TransactionRepository;
import feign.FeignException.GatewayTimeout;
import feign.RetryableException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferService {
  private final ExternalBankClient externalBankClient;
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final RabbitTemplate rabbitTemplate;
  private final AmqpProperties amqpProperties;

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
    try {

      final PostExternalTransferResponse transferResponse = externalBankClient.transfer(
          PostExternalTransferRequest.builder()
              .amount(amount)
              .fromAccountNumber(sendingAccountNumber)
              .toAccountNumber(receivingAccountNumber)
              .build()
      );

      final String errorCode = transferResponse.getErrorCode();
      if (StringUtils.isNotBlank(errorCode)) {
        switch (errorCode) {
          case RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE -> throw ExternalTransferException.RECEIVING_ACCOUNT_NOT_FOUND;
          case RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE -> throw ExternalTransferException.RECEIVING_ACCOUNT_INACTIVE;
        }
      }

      sendingAccount.setBalance(currentBalance.subtract(amount));
      accountRepository.save(sendingAccount);
      transactionRepository.save(
          TransactionEntity.builder()
              .accountNumber(sendingAccountNumber)
              .amount(amount)
              .counterpartyAccountNumber(receivingAccountNumber)
              .type(TransactionType.CREDIT)
              .build()
      );

      rabbitTemplate.convertAndSend(
          amqpProperties.getNotifyBalanceQueue(),
          BalanceChangeDto.builder().accountNumber(sendingAccountNumber)
              .latestBalance(sendingAccount.getBalance())
              .amount(amount)
              .build()
      );

      return sendingAccount;
    } catch (GatewayTimeout | RetryableException exception) {
      log.error("call external api error", exception);
      throw ExternalTransferException.TIMEOUT;
    }
  }

}
