package fr.spoutnik87.musicbot_rest.security;

import fr.spoutnik87.musicbot_rest.constant.RoleEnum;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new UsernameNotFoundException(email);
    }

    return new fr.spoutnik87.musicbot_rest.security.UserDetails(
        user, getAuthorities(user.getRole().getLvl()));
  }

  private Collection<? extends GrantedAuthority> getAuthorities(int lvl) {
    ArrayList<GrantedAuthority> authorities = new ArrayList<>();
    switch (lvl) {
      case 1:
        authorities.add(new SimpleGrantedAuthority(RoleEnum.ADMIN.getName()));
        break;
      default:
        authorities.add(new SimpleGrantedAuthority(RoleEnum.USER.getName()));
    }
    return authorities;
  }
}
