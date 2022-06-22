package bkit.solutions.springbootstudy.clients.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PostExternalTransferRequest {
  private String from;
  private String to;
  private BigDecimal amount;
}
