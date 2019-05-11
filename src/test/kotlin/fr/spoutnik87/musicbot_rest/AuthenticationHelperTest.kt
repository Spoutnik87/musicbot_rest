package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import fr.spoutnik87.musicbot_rest.util.WithSecurityContextTestExecutionListener
import fr.spoutnik87.musicbot_rest.util.WithUserDetails
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BCryptPasswordEncoder::class, WebSecurityTestConfig::class])
@TestExecutionListeners(listeners = [
    WithSecurityContextTestExecutionListener::class,
    DependencyInjectionTestExecutionListener::class,
    MockitoTestExecutionListener::class
])
class AuthenticationHelperTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun init() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(
                        User(
                                "token",
                                "user@test.com",
                                "Nickname",
                                "Firstname",
                                "Lastname",
                                bCryptPasswordEncoder.encode("password"),
                                Role("token", "USER", 2)))
        Mockito.`when`(userRepository.findByEmail("admin@test.com"))
                .thenReturn(
                        User(
                                "token2",
                                "admin@test.com",
                                "Nickname",
                                "Firstname",
                                "Lastname",
                                bCryptPasswordEncoder.encode("password"),
                                Role("token2", "ADMIN", 1)))
    }

    @Test
    fun isAuthenticated_NotAuthenticated_ReturnFalse() {
        assertFalse(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun isAuthenticated_Authenticated_ReturnTrue() {
        assertTrue(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUserDetails_Authenticated_ReturnUserDetails() {
        val userDetails = AuthenticationHelper.getAuthenticatedUserDetails()
        assertEquals("user@test.com", userDetails?.username)
        assertNotNull(userDetails?.password)
        assertNotNull(userDetails?.authorities)
        assertEquals(RoleEnum.USER.value,
                (userDetails?.authorities!!.toTypedArray()[0] as SimpleGrantedAuthority).authority)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUserEmail_Authenticated_ReturnUserEmail() {
        assertEquals("user@test.com", AuthenticationHelper.getAuthenticatedUserEmail())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUserAuthorities_Authenticated_ReturnUserAuthorities() {
        val simpleGrantedAuthorities = AuthenticationHelper.getAuthenticatedUserAuthorities()
        assertNotNull(simpleGrantedAuthorities)
        assertEquals(RoleEnum.USER.value, simpleGrantedAuthorities?.get(0)?.authority)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun isAuthenticatedUserInRole_AuthenticatedUser_ReturnTrueIfUser() {
        assertTrue(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.USER))
        assertFalse(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.ADMIN))
    }

    @Test
    fun getAuthenticatedUser_NotAuthenticated_ReturnNull() {
        assertNull(AuthenticationHelper.getAuthenticatedUser())
    }

    @Test
    @WithUserDetails("user@test.com")
    fun getAuthenticatedUser_Authenticated_ReturnUser() {
        assertNotNull(AuthenticationHelper.getAuthenticatedUser())
    }
}