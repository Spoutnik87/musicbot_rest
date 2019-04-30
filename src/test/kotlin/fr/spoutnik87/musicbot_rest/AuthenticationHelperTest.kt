package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BCryptPasswordEncoder::class, WebSecurityTestConfig::class])
class AuthenticationHelperTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @Test
    fun isAuthenticatedWhenUserNotAuthenticated() {
        assertFalse(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun isAuthenticatedWhenUserAuthenticated() {
        assertTrue(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUserDetails() {
        val userDetails = AuthenticationHelper.getAuthenticatedUserDetails()
        assertEquals("user@test.com", userDetails?.username)
        assertNotNull(userDetails?.password)
        assertNotNull(userDetails?.authorities)
        assertEquals(RoleEnum.USER.value,
                (userDetails?.authorities!!.toTypedArray()[0] as SimpleGrantedAuthority).authority)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUserEmail() {
        assertEquals("user@test.com", AuthenticationHelper.getAuthenticatedUserEmail())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUserAuthorities() {
        val simpleGrantedAuthorities = AuthenticationHelper.getAuthenticatedUserAuthorities()
        assertNotNull(simpleGrantedAuthorities)
        assertEquals(RoleEnum.USER.value, simpleGrantedAuthorities?.get(0)?.authority)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun isAuthenticatedUserInRole() {
        assertTrue(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.USER))
        assertFalse(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.ADMIN))
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUser() {
        assertNotNull(AuthenticationHelper.getAuthenticatedUser())
    }
}