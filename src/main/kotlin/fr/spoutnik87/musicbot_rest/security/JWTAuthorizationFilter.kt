package fr.spoutnik87.musicbot_rest.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthorizationFilter(
        val userDetailsService: UserDetailsServiceImpl,
        authManager: AuthenticationManager,
        val securityConfiguration: SecurityConfiguration
) : BasicAuthenticationFilter(authManager) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val header: String? = req.getHeader(this.securityConfiguration.headerString)
        if (header == null || !header.startsWith(this.securityConfiguration.tokenPrefix)) {
            chain.doFilter(req, res)
            return
        }
        try {
            val authentication = getAuthentication(req)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
        }
        chain.doFilter(req, res)
    }

    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(this.securityConfiguration.headerString)
        if (token != null) {
            val user = JWT.require(Algorithm.HMAC512(securityConfiguration.secret.toByteArray()))
                    .build()
                    .verify(token.replace(this.securityConfiguration.tokenPrefix, ""))
                    .subject

            return if (user != null) {
                var userDetails = this.userDetailsService.loadUserByUsername(user)
                UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails?.authorities)
            } else null
        }
        return null
    }
}