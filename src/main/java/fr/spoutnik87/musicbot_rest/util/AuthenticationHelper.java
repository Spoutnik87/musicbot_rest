package fr.spoutnik87.musicbot_rest.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class AuthenticationHelper {

  public static boolean isAuthenticatedUserInRole(String role) {
    Collection<? extends GrantedAuthority> l =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    for (GrantedAuthority ga : l) {
      if (ga.getAuthority().equals(role)) {
        return true;
      }
    }
    return false;
  }

  public static String getAuthenticatedUserLogin() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
  }
}
