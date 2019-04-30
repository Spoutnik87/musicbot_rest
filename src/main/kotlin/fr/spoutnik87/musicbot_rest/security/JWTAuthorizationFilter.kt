package fr.spoutnik87.musicbot_rest.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthorizationFilter(
        val userDetailsService: UserDetailsServiceImpl,
        authManager: AuthenticationManager,
        val securityConfiguration: SecurityConfiguration
) : BasicAuthenticationFilter(authManager) {

    @Value("\${security.secret}")
    private lateinit var secret: String

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val header: String? = req.getHeader(this.securityConfiguration.headerString)
        if (header == null || !header?.startsWith(this.securityConfiguration.tokenPrefix)) {
            chain.doFilter(req, res)
            return
        }
        val authentication = getAuthentication(req)
        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(req, res)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(this.securityConfiguration.headerString)
        if (token != null) {
            val user = JWT.require(Algorithm.HMAC512(secret.toByteArray()))
                    .build()
                    .verify(token.replace(this.securityConfiguration.tokenPrefix, ""))
                    .subject

            return if (user != null) {
                var userDetails = this.userDetailsService.loadUserByUsername(user)
                var authentication = UsernamePasswordAuthenticationToken(
                        user, null, userDetails.authorities)
                authentication.details = userDetails
                authentication
            } else null
        }
        return null
    }
}