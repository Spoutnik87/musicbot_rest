package fr.spoutnik87.musicbot_rest.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC512
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.viewmodel.UserViewModel
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(
        private val userDetailsService: UserDetailsServiceImpl,
        private val authManager: AuthenticationManager,
        private val securityConfiguration: SecurityConfiguration
) : UsernamePasswordAuthenticationFilter() {

    @Throws(RuntimeException::class)
    override fun attemptAuthentication(req: HttpServletRequest?, res: HttpServletResponse?): Authentication {
        try {
            val creds = ObjectMapper().readValue(req?.inputStream, User::class.java)
            return authManager.authenticate(
                    UsernamePasswordAuthenticationToken(creds.email, creds.password,
                            userDetailsService.loadUserByUsername(creds.email)?.authorities))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun successfulAuthentication(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain?, auth: Authentication?) {
        val username = (auth?.principal as org.springframework.security.core.userdetails.UserDetails).username
        val token = JWT.create()
                .withSubject(username)
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
        val user = userDetailsService.loadUserByUsername(username)?.user
        res.writer.print(mapper.writerWithView(Views.Companion.Mixed::class.java)
                .writeValueAsString(UserViewModel.from(user!!)))
        res.writer.flush()
    }
}