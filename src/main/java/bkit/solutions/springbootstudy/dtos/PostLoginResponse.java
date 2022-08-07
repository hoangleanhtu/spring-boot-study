package bkit.solutions.springbootstudy.dtos;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLoginResponse {
  private String accessToken;
  private ZonedDateTime expireAt;
}
