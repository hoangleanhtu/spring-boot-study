package bkit.solutions.springbootstudy.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("access-token")
public class AccessTokenInfo {
  @Id
  private String accessToken;
  private String username;
  private UserRole role;

  @TimeToLive
  private Long expiration;
}
