package fr.spoutnik87.musicbot_rest.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * Tokens expires in 5 minutes
 */
private const val EXPIRATION_TIME = 300000

@Service
class TokenService {

    @Autowired
    private lateinit var securityConfiguration: SecurityConfiguration

    /**
     * @param id User UUID
     */
    fun createServerJoinToken(id: String): String {
        val serverJoinToken = ServerJoinToken(id)
        return JWT.create()
                .withClaim("type", serverJoinToken.type)
                .withClaim("id", serverJoinToken.id)
                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))
    }

    /**
     * @param id Server UUID
     */
    fun createServerLinkToken(id: String): String {
        val serverLinkToken = ServerLinkToken(id)
        return JWT.create()
                .withClaim("type", serverLinkToken.type)
                .withClaim("id", serverLinkToken.id)
                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))
    }

    fun decodeServerJoinToken(encodedToken: String): ServerJoinToken? {
        var token: ServerJoinToken? = null
        try {
            val decodedToken = JWT.require(Algorithm.HMAC512(securityConfiguration.secret.toByteArray()))
                    .build()
                    .verify(encodedToken)
            if (decodedToken.expiresAt.after(Date()) && decodedToken.getClaim("type").asString() == TokenTypeEnum.SERVER_JOIN_TOKEN.name) {
                token = ServerJoinToken(decodedToken.getClaim("id").asString())
            }
        } catch (e: Exception) {
        }
        return token

    }

    fun decodeServerLinkToken(encodedToken: String): ServerLinkToken? {
        var token: ServerLinkToken? = null
        try {
            val decodedToken = JWT.require(Algorithm.HMAC512(securityConfiguration.secret.toByteArray()))
                    .build()
                    .verify(encodedToken)
            if (decodedToken.expiresAt.after(Date()) && decodedToken.getClaim("type").asString() == TokenTypeEnum.SERVER_LINK_TOKEN.name) {
                token = ServerLinkToken(decodedToken.getClaim("id").asString())
            }
        } catch (e: Exception) {
        }
        return token
    }
}

enum class TokenTypeEnum {
    SERVER_JOIN_TOKEN,
    SERVER_LINK_TOKEN
}

abstract class Token(
        open val type: String
)

data class ServerJoinToken(
        /**
         * User UUID
         */
        val id: String
) : Token(TokenTypeEnum.SERVER_JOIN_TOKEN.name)

data class ServerLinkToken(
        /**
         * Server UUID
         */
        val id: String
) : Token(TokenTypeEnum.SERVER_LINK_TOKEN.name)