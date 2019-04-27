package fr.spoutnik87.musicbot_rest.util;

import fr.spoutnik87.musicbot_rest.constant.RoleEnum;
import fr.spoutnik87.musicbot_rest.model.Role;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityTestConfig extends WebSecurityConfigurerAdapter {

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Bean
  public SecurityConfiguration securityConfiguration() {
    return new SecurityConfiguration();
  }

  @Bean
  public UserDetailsServiceImpl userDetailsService() {
    Role roleUser = new Role("token", "USER", 2);
    User user =
        new User(
            "token",
            "user@test.com",
            "Nickname",
            "Firstname",
            "Lastname",
            bCryptPasswordEncoder.encode("password"),
            roleUser);
    UserDetails basicUser =
        new UserDetails(user, Arrays.asList(new SimpleGrantedAuthority(RoleEnum.USER.getName())));

    Role roleAdmin = new Role("token2", "ADMIN", 1);
    User userAdmin =
            new User(
                    "token2",
                    "admin@test.com",
                    "Nickname",
                    "Firstname",
                    "Lastname",
                    bCryptPasswordEncoder.encode("password"),
                    roleAdmin);
    UserDetails adminUser =
            new UserDetails(
                    userAdmin, Arrays.asList(new SimpleGrantedAuthority(RoleEnum.ADMIN.getName())));
    return new InMemoryUserDetailsManager(Arrays.asList(basicUser, adminUser));
  }

  @Autowired private UserDetailsServiceImpl userDetailsService;
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
