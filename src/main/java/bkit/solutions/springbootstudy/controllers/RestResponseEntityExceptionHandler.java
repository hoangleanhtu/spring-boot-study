package bkit.solutions.springbootstudy.controllers;

import bkit.solutions.springbootstudy.dtos.ErrorResponse;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes;
import bkit.solutions.springbootstudy.exceptions.ExternalTransferException;
import bkit.solutions.springbootstudy.exceptions.InvalidUsernameAndPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({ExternalTransferException.class})
  ResponseEntity<ErrorResponse> handleExternalTransferError(ExternalTransferException exception) {
    if (ExternalTransferErrorCodes.TIMEOUT_ERROR_CODE.equals(exception.getErrorCode())) {
      return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
          .build();
    }

    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(exception.getErrorCode(), exception.getMessage()));
  }

  @ExceptionHandler({InvalidUsernameAndPasswordException.class})
  ResponseEntity<ErrorResponse> handleInvalidUsernameAndPasswordException(
      InvalidUsernameAndPasswordException exception) {
    final HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
    return ResponseEntity.status(unauthorized)
        .body(new ErrorResponse(unauthorized.value() + "",
            "Invalid username or password"));
  }
}
