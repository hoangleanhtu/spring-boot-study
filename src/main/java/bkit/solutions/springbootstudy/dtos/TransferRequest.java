package bkit.solutions.springbootstudy.dtos;

import java.math.BigDecimal;

public record TransferRequest(
    String fromAccountNumber,
    String toAccountNumber,
    BigDecimal amount
) { }
