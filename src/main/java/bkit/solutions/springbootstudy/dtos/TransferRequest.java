package bkit.solutions.springbootstudy.dtos;

import java.math.BigDecimal;

public record TransferRequest(
    String sendingAccountNumber,
    String receivingAccountNumber,
    BigDecimal amount
) { }
