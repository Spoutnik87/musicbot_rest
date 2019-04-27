package fr.spoutnik87.musicbot_rest.util;

import fr.spoutnik87.musicbot_rest.security.UserDetails;
import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryUserDetailsManager extends UserDetailsServiceImpl {

    private Map<String, UserDetails> userDetailsMap;

    public InMemoryUserDetailsManager(List<UserDetails> userDetailsList) {
        userDetailsMap = new HashMap<>();
        for (UserDetails userDetails : userDetailsList) {
            this.userDetailsMap.put(userDetails.getUsername(), userDetails);
        }
    }

    public void createUser(UserDetails userDetails) {
        if (userDetailsMap.containsKey(userDetails.getUsername())) {
            return;
        }
        userDetailsMap.put(userDetails.getUsername(), userDetails);
    }

    public void deleteUser(String username) {
        if (!userDetailsMap.containsKey(username)) {
            return;
        }
        userDetailsMap.remove(username);
    }

    public boolean userExists(String username) {
        return userDetailsMap.containsKey(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (!userDetailsMap.containsKey(username)) {
            throw new UsernameNotFoundException(username);
        }
        return userDetailsMap.get(username);
    }
}
