package fr.spoutnik87.musicbot_rest.util;

import fr.spoutnik87.musicbot_rest.security.JWTAuthenticationFilter;
import fr.spoutnik87.musicbot_rest.security.JWTAuthorizationFilter;
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration;
import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityTestConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsServiceImpl userDetailsServiceImpl() {
    return new UserDetailsServiceImpl();
  }

  @Bean
  public SecurityConfiguration securityConfiguration() {
    return new SecurityConfiguration();
  }

  @Autowired private UserDetailsServiceImpl userDetailsService;
  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired private SecurityConfiguration securityConfiguration;

  private static final String[] AUTH_WHITELIST = {
    // -- swagger ui
    "/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**"
  };

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

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser("user@test.com")
        .password("password")
        .roles("USER")
        .and()
        .withUser("admin@test.com")
        .password("password")
        .roles("ADMIN");
  }
}
