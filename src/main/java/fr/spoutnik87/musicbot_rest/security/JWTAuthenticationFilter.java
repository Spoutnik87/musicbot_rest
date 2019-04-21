package fr.spoutnik87.musicbot_rest.security;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.model.Views;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private UserDetailsServiceImpl userDetailsService;
  private AuthenticationManager authenticationManager;

  private SecurityConfiguration securityConfiguration;

  public JWTAuthenticationFilter(
      UserDetailsServiceImpl userDetailsService,
      AuthenticationManager authenticationManager,
      SecurityConfiguration securityConfiguration) {
    this.userDetailsService = userDetailsService;
    this.authenticationManager = authenticationManager;
    this.securityConfiguration = securityConfiguration;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException {
    try {
      fr.spoutnik87.musicbot_rest.security.User creds =
          new ObjectMapper()
              .readValue(req.getInputStream(), fr.spoutnik87.musicbot_rest.security.User.class);

      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              creds.getEmail(),
              creds.getPassword(),
              userDetailsService.loadUserByUsername(creds.getEmail()).getAuthorities()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) {
    String token =
        JWT.create()
            .withSubject(((UserDetails) auth.getPrincipal()).getUsername())
            .withExpiresAt(
                new Date(
                    System.currentTimeMillis() + this.securityConfiguration.getExpirationTime()))
            .sign(HMAC512(this.securityConfiguration.getSecret().getBytes()));
    res.addHeader("Access-Control-Expose-Headers", "Authorization");
    res.addHeader(
        this.securityConfiguration.getHeaderString(),
        this.securityConfiguration.getTokenPrefix() + token);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    try {
      ObjectMapper mapper = new ObjectMapper();
      User u = ((UserDetails) auth.getPrincipal()).getUser();
      mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
      String result = mapper.writerWithView(Views.Public.class).writeValueAsString(u);
      res.getWriter().print(result);
      res.getWriter().flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
