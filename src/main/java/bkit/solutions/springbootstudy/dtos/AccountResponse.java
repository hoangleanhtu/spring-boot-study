package bkit.solutions.springbootstudy.dtos;

import java.math.BigDecimal;

public record AccountResponse(String accountNumber, BigDecimal availableBalance) {

}
