package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.constants.TransactionApiEndpoints.EXTERNAL_TRANSFER_V1;

import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferException;
import bkit.solutions.springbootstudy.services.ExternalTransferService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TransactionApiEndpoints.PREFIX)
public record ExternalTransferController(ExternalTransferService externalTransferService) {

  @PostMapping(EXTERNAL_TRANSFER_V1)
  public AccountEntity transferToExternalAccount(@RequestBody TransferRequest transferRequest)
      throws ExternalTransferException {
    return externalTransferService.transfer(transferRequest);
  }
}
