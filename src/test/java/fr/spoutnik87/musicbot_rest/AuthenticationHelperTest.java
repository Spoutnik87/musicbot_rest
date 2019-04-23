package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BCryptTestConfig.class, SpringSecurityTestConfig.class})
public class AuthenticationHelperTest {

  @Test
  public void isAuthenticatedWhenUserNotAuthenticated() {
    assertFalse(AuthenticationHelper.isAuthenticated());
  }

  @Test
  @WithUserDetails("user@test.com")
  public void isAuthenticatedWhenUserAuthenticated() {
    assertTrue(AuthenticationHelper.isAuthenticated());
  }

  @Test
  @WithUserDetails("user@test.com")
  public void getAuthenticatedUserDetails() {
    UserDetails userDetails = AuthenticationHelper.getAuthenticatedUserDetails();
    assertEquals("user@test.com", userDetails.getUsername());
    assertNotNull(userDetails.getPassword());
    assertNotNull(userDetails.getAuthorities());
    assertTrue(
        ((SimpleGrantedAuthority) (userDetails.getAuthorities().toArray()[0]))
            .getAuthority()
            .equals("USER"));
  }

  @Test
  @WithUserDetails("user@test.com")
  public void getAuthenticatedUserEmail() {
    assertEquals("user@test.com", AuthenticationHelper.getAuthenticatedUserEmail());
  }

  @Test
  @WithUserDetails("user@test.com")
  public void getAuthenticatedUserAuthorities() {
    List<SimpleGrantedAuthority> simpleGrantedAuthorities =
        AuthenticationHelper.getAuthenticatedUserAuthorities();
    assertNotNull(simpleGrantedAuthorities);
    assertEquals("USER", simpleGrantedAuthorities.get(0).getAuthority());
  }

  @Test
  @WithUserDetails("user@test.com")
  public void isAuthenticatedUserInRole() {
    assertTrue(AuthenticationHelper.isAuthenticatedUserInRole("USER"));
    assertFalse(AuthenticationHelper.isAuthenticatedUserInRole("ADMIN"));
  }
}
