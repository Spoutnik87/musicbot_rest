package fr.spoutnik87.musicbot_rest.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC512
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import fr.spoutnik87.musicbot_rest.model.Views
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(
        private val userDetailsService: UserDetailsServiceImpl,
        private val authManager: AuthenticationManager,
        private val securityConfiguration: SecurityConfiguration
) : UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(req: HttpServletRequest?, res: HttpServletResponse?): Authentication {
        val creds = ObjectMapper().readValue(req?.inputStream, User::class.java)
        return authManager.authenticate(
                UsernamePasswordAuthenticationToken(creds.email, creds.password,
                        userDetailsService.loadUserByUsername(creds.email).authorities))
    }

    override fun successfulAuthentication(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain?, auth: Authentication?) {
        val token = JWT.create()
                .withSubject((auth?.principal as UserDetails).username)
                .withExpiresAt(Date(System.currentTimeMillis() + this.securityConfiguration.expirationTime))
                .sign(HMAC512(this.securityConfiguration.secret.toByteArray()))
        res.addHeader("Access-Control-Expose-Headers", "Authorization")
        res.addHeader(
                this.securityConfiguration.headerString,
                this.securityConfiguration.tokenPrefix + token)
        res.contentType = "application/json"
        res.characterEncoding = "UTF-8"

        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        res.writer.print(mapper.writerWithView(Views.Companion.Public::class.java)
                .writeValueAsString((auth.principal as UserDetails).user))
        res.writer.flush()
    }
}