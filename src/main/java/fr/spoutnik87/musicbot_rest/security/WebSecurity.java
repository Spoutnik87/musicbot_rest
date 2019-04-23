package fr.spoutnik87.musicbot_rest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

  private UserDetailsServiceImpl userDetailsService;
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  private SecurityConfiguration securityConfiguration;

  private static final String[] AUTH_WHITELIST = {
    // -- swagger ui
    "/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**"
  };

  public WebSecurity(
      UserDetailsServiceImpl userDetailsService,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      SecurityConfiguration securityConfiguration) {
    this.userDetailsService = userDetailsService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.securityConfiguration = securityConfiguration;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .csrf()
        .disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, securityConfiguration.getSignUpUrl())
        .permitAll()
        .antMatchers(AUTH_WHITELIST)
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .addFilter(
            new JWTAuthenticationFilter(
                userDetailsService, authenticationManager(), securityConfiguration))
        .addFilter(
            new JWTAuthorizationFilter(
                userDetailsService, authenticationManager(), securityConfiguration))
        // this disables session creation on Spring Security
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(
        Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(
        Arrays.asList(
            "X-Auth-Token",
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"));
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
