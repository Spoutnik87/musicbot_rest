package fr.spoutnik87.musicbot_rest.security;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class SecurityConfiguration {
  @Value("${security.secret}")
  private String secret;
  private final long expirationTime = 864_000_000;
  private final String tokenPrefix = "Bearer ";
  private final String headerString = "Authorization";
  private final String signUpUrl = "/user";
}
