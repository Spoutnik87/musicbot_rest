package fr.spoutnik87.musicbot_rest.security;

import fr.spoutnik87.musicbot_rest.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class UserDetails extends org.springframework.security.core.userdetails.User {
    private User user;

    public UserDetails(User user, Collection<? extends GrantedAuthority> grantedAuthorities) {
        super(user.getEmail(), user.getPassword(), grantedAuthorities);
        this.user = user;
    }
}
