package fr.spoutnik87.musicbot_rest.security

import fr.spoutnik87.musicbot_rest.model.User
import org.springframework.security.core.GrantedAuthority

data class UserDetails(
        var user: User,
        var grantedAuthorities: Collection<GrantedAuthority>
) : org.springframework.security.core.userdetails.User(user.email, user.password, grantedAuthorities) {
}