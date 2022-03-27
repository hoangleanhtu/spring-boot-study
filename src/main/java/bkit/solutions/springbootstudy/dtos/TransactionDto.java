package bkit.solutions.springbootstudy.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransactionDto {

  private Long id;
  private String accountNumber;
  private String counterpartyAccountNumber;
  private BigDecimal amount;
  private TransactionType type;
}
