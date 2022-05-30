package bkit.solutions.springbootstudy.exceptions;

import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.NOT_ENOUGH_BALANCE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ExternalTransferException extends Exception {

  public static final ExternalTransferException NOT_ENOUGH_BALANCE = new ExternalTransferException(
      NOT_ENOUGH_BALANCE_ERROR_CODE);
  public static final ExternalTransferException RECEIVING_ACCOUNT_NOT_FOUND = new ExternalTransferException(
      RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE);
  public static final ExternalTransferException RECEIVING_ACCOUNT_INACTIVE = new ExternalTransferException(
      RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE);
  public static final ExternalTransferException TIMEOUT = new ExternalTransferException("ERR000");

  private final String errorCode;
}
