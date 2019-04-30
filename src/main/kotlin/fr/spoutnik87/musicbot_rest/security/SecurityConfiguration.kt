package fr.spoutnik87.musicbot_rest.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecurityConfiguration {
    @Value("\${security.secret}")
    lateinit var secret: String

    val expirationTime: Long = 864000000
    val tokenPrefix = "Bearer "
    val headerString = "Authorization"
    val signUpUrl = "/user"
}