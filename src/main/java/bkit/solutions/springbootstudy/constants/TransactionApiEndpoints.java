package bkit.solutions.springbootstudy.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionApiEndpoints {
  public static final String PREFIX = "/transactions";
  public static final String TRANSFER_V1 = "/v1/transfer";
  public static final String EXTERNAL_TRANSFER_V1 = "v1/transfer/external";
}
