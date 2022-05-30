package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.clients.ExternalBankClient;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferException;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
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

  @Transactional
  public AccountEntity transfer(TransferRequest transferRequest) throws ExternalTransferException {
    // TODO tu.hoang implement
    final AccountEntity sendingAccount = accountRepository.findByAccountNumber(
        transferRequest.sendingAccountNumber()).get();
    final BigDecimal amount = transferRequest.amount();
    final BigDecimal currentBalance = sendingAccount.getBalance();
    if (currentBalance.compareTo(amount) < 0) {
      throw ExternalTransferException.NOT_ENOUGH_BALANCE;
    }
    return null;
  }

}
