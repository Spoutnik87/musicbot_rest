package fr.spoutnik87.musicbot_rest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

  private UserDetailsServiceImpl userDetailsService;
  private SecurityConfiguration securityConfiguration;

  @Value("${security.secret}")
  private String secret;

  public JWTAuthorizationFilter(
      UserDetailsServiceImpl userDetailsService,
      AuthenticationManager authManager,
      SecurityConfiguration securityConfiguration) {
    super(authManager);
    this.userDetailsService = userDetailsService;
    this.securityConfiguration = securityConfiguration;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String header = req.getHeader(this.securityConfiguration.getHeaderString());

    if (header == null || !header.startsWith(this.securityConfiguration.getTokenPrefix())) {
      chain.doFilter(req, res);
      return;
    }

    UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(req, res);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token = request.getHeader(this.securityConfiguration.getHeaderString());
    if (token != null) {
      // parse the token.
      String user =
          JWT.require(Algorithm.HMAC512(secret.getBytes()))
              .build()
              .verify(token.replace(this.securityConfiguration.getTokenPrefix(), ""))
              .getSubject();

      if (user != null) {
        return new UsernamePasswordAuthenticationToken(
            user, null, this.userDetailsService.loadUserByUsername(user).getAuthorities());
      }
      return null;
    }
    return null;
  }
}
