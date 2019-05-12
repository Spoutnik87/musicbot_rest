package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BCryptPasswordEncoder::class, WebSecurityTestConfig::class])
class AuthenticationHelperTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Test
    fun isAuthenticated_NotAuthenticated_ReturnFalse() {
        assertFalse(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun isAuthenticated_Authenticated_ReturnTrue() {
        assertTrue(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getAuthenticatedUserDetails_Authenticated_ReturnUserDetails() {
        val userDetails = AuthenticationHelper.getAuthenticatedUserDetails()
        assertEquals("user@test.com", userDetails?.username)
        assertNotNull(userDetails?.password)
        assertNotNull(userDetails?.authorities)
        assertEquals(RoleEnum.USER.value,
                (userDetails?.authorities!!.toTypedArray()[0] as SimpleGrantedAuthority).authority)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getAuthenticatedUserEmail_Authenticated_ReturnUserEmail() {
        assertEquals("user@test.com", AuthenticationHelper.getAuthenticatedUserEmail())
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getAuthenticatedUserAuthorities_Authenticated_ReturnUserAuthorities() {
        val simpleGrantedAuthorities = AuthenticationHelper.getAuthenticatedUserAuthorities()
        assertNotNull(simpleGrantedAuthorities)
        assertEquals(RoleEnum.USER.value, simpleGrantedAuthorities?.get(0)?.authority)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun isAuthenticatedUserInRole_AuthenticatedUser_ReturnTrueIfUser() {
        assertTrue(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.USER))
        assertFalse(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.ADMIN))
    }
}