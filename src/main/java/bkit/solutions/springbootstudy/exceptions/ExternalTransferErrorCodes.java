package bkit.solutions.springbootstudy.exceptions;

public interface ExternalTransferErrorCodes {
  String NOT_ENOUGH_BALANCE_ERROR_CODE = "ERR001";
  String RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE = "ERR004";
  String RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE = "ERR005";
  String TIMEOUT_ERROR_CODE = "ERR000";
}
