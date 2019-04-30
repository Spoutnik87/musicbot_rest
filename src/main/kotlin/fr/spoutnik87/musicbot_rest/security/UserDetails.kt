package fr.spoutnik87.musicbot_rest.security

import fr.spoutnik87.musicbot_rest.model.User
import org.springframework.security.core.GrantedAuthority

class UserDetails(
        val user: User,
        val grantedAuthorities: Collection<GrantedAuthority>
) : org.springframework.security.core.userdetails.User(user.email, user.password, grantedAuthorities) {
}