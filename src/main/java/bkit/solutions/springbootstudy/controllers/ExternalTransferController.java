package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.constants.TransactionApiEndpoints.EXTERNAL_TRANSFER_V1;

import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import bkit.solutions.springbootstudy.dtos.TransferRequest;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferException;
import bkit.solutions.springbootstudy.services.ExternalTransferService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

  @ExceptionHandler({ExternalTransferException.class})
  ResponseEntity<Object> handleExternalTransferError(ExternalTransferException exception) {
    if (ExternalTransferErrorCodes.TIMEOUT_ERROR_CODE.equals(exception.getErrorCode())) {
      return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
          .build();
    }

    return ResponseEntity.badRequest()
        .body(Map.of("errorCode", exception.getErrorCode()));
  }
}
