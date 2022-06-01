package bkit.solutions.springbootstudy.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceChangeDto {
  private String accountNumber;
  private BigDecimal latestBalance;
  private BigDecimal amount;
}
