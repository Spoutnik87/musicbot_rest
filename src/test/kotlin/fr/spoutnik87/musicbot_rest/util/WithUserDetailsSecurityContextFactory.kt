package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.util.Assert


class WithUserDetailsSecurityContextFactory : WithSecurityContextFactory<WithCustomUserDetails> {

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    /*@Autowired
    fun WithUserDetailsSecurityContextFactory(userDetailsService: UserDetailsService) {
        this.userDetailsService = userDetailsService
    }*/

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    override fun createSecurityContext(withUser: WithCustomUserDetails): SecurityContext {
        val username = withUser.value
        Assert.hasLength(username, "value() must be non-empty String")
        val principal = userDetailsService.loadUserByUsername(username)
        val authentication = UsernamePasswordAuthenticationToken(principal, principal?.password, principal?.authorities)
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        return context
    }
}