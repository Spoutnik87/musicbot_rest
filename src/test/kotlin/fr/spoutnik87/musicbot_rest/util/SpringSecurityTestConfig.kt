package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.security.UserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import java.util.*

class SpringSecurityTestConfig {

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Bean
    fun userDetailsService(): UserDetailsService {
        val role = Role("token", "USER", 2)
        val user = User("token", "user@test.com", "Nickname", "Firstname", "Lastname", bCryptPasswordEncoder.encode("password"))
        user.role = role
        val basicUser = UserDetails(user, Arrays.asList(SimpleGrantedAuthority(RoleEnum.USER.value)))
        return InMemoryUserDetailsManager(listOf(basicUser))
    }
}