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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityTestConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public SecurityConfiguration securityConfiguration() {
    return new SecurityConfiguration();
  }

  @Bean
  public UserDetailsServiceImpl userDetailsService() {
    return new UserDetailsServiceImpl();
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

  /*@Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser("usertest@test.com")
        .password(bCryptPasswordEncoder.encode("usertest"))
        .roles("USER")
        .and()
        .withUser("admintest@test.com")
        .password(bCryptPasswordEncoder.encode("admintest"))
        .roles("ADMIN");
  }*/

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    /*User user =
        new User(
            "token",
            "user@test.com",
            "Nickname",
            "Firstnamee",
            "Lastname",
            bCryptPasswordEncoder.encode("password"),
            new Role("token", RoleEnum.USER.getName(), 2));
    ArrayList<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(RoleEnum.USER.getName()));
    Mockito.when(userDetailsService.loadUserByUsername("user@test.com"))
        .thenReturn(new UserDetails(user, authorities));*/
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
