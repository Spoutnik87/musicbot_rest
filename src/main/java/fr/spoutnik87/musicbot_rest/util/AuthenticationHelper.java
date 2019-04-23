package fr.spoutnik87.musicbot_rest.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationHelper {

  public static boolean isAuthenticatedUserInRole(String role) {
    if (!isAuthenticated()) {
      return false;
    }
    List<SimpleGrantedAuthority> simpleGrantedAuthorities = getAuthenticatedUserAuthorities();
    for (SimpleGrantedAuthority simpleGrantedAuthority : simpleGrantedAuthorities) {
      if (simpleGrantedAuthority.getAuthority().equals(role)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if an user is authenticated, false if not.
   *
   * @return Authentication status.
   */
  public static boolean isAuthenticated() {
    return SecurityContextHolder.getContext().getAuthentication() != null;
  }

  /**
   * Return informations about connected user or null if no user is connected.
   *
   * @return User informations
   */
  public static UserDetails getAuthenticatedUserDetails() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    return (UserDetails) authentication.getPrincipal();
  }

  /**
   * Return email of connected user or null if no user is connected.
   *
   * @return User email
   */
  public static String getAuthenticatedUserEmail() {
    UserDetails userDetails = getAuthenticatedUserDetails();
    if (userDetails == null) {
      return null;
    }
    return userDetails.getUsername();
  }

  public static List<SimpleGrantedAuthority> getAuthenticatedUserAuthorities() {
    List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
    for (GrantedAuthority grantedAuthority : getAuthenticatedUserDetails().getAuthorities()) {
      simpleGrantedAuthorities.add((SimpleGrantedAuthority) grantedAuthority);
    }
    return simpleGrantedAuthorities;
  }
}
