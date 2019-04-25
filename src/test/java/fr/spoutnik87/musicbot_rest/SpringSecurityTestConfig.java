package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.model.Role;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.security.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;

public class SpringSecurityTestConfig {

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Bean
  @Primary
  public UserDetailsService userDetailsService() {
    User user = new User();
    user.setEmail("user@test.com");
    user.setFirstname("Firstname");
    user.setLastname("Lastname");
    user.setNickname("Nickname");
    user.setPassword(bCryptPasswordEncoder.encode("password"));
    user.setRole(Role.USER);
    UserDetails basicUser =
        new UserDetails(user, Arrays.asList(new SimpleGrantedAuthority(Role.USER.getName())));

    return new InMemoryUserDetailsManager(Arrays.asList(basicUser));
  }
}
