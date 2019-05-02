package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.security.UserDetails
import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import fr.spoutnik87.musicbot_rest.util.WithCustomUserDetails
import fr.spoutnik87.musicbot_rest.util.WithSecurityContextTestExecutionListener
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BCryptPasswordEncoder::class, WebSecurityTestConfig::class])
@TestExecutionListeners(listeners = [WithSecurityContextTestExecutionListener::class, DependencyInjectionTestExecutionListener::class])
class AuthenticationHelperTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun init() {
        System.out.println("aaa")
        val roleUser = Role("token", "USER", 2)
        val user = User(
                "token",
                "user@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                roleUser)
        val basicUser = UserDetails(user, listOf(SimpleGrantedAuthority(RoleEnum.USER.value)))

        val roleAdmin = Role("token2", "ADMIN", 1)
        val userAdmin = User(
                "token2",
                "admin@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                roleAdmin)
        val adminUser = UserDetails(
                userAdmin, listOf(SimpleGrantedAuthority(RoleEnum.ADMIN.value)))

        Mockito.`when`(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(basicUser)
        Mockito.`when`(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(adminUser)
        /*val roleUser = Role("token", "USER", 2)
        val user = User(
                "token",
                "user@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                roleUser)
        val basicUser = UserDetails(user, listOf(SimpleGrantedAuthority(RoleEnum.USER.value)))

        val roleAdmin = Role("token2", "ADMIN", 1)
        val userAdmin = User(
                "token2",
                "admin@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                roleAdmin)
        val adminUser = UserDetails(
                userAdmin, listOf(SimpleGrantedAuthority(RoleEnum.ADMIN.value)))

        userDetailsService.deleteAllUsers()
        userDetailsService.createUser(basicUser)
        userDetailsService.createUser(adminUser)*/
    }

    @Test
    fun isAuthenticatedWhenUserNotAuthenticated() {
        assertFalse(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithCustomUserDetails("user@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    fun isAuthenticatedWhenUserAuthenticated() {
        assertTrue(AuthenticationHelper.isAuthenticated())
    }

    @Test
    @WithCustomUserDetails("user@test.com")
    fun getAuthenticatedUserDetails() {
        val userDetails = AuthenticationHelper.getAuthenticatedUserDetails()
        assertEquals("user@test.com", userDetails?.username)
        assertNotNull(userDetails?.password)
        assertNotNull(userDetails?.authorities)
        assertEquals(RoleEnum.USER.value,
                (userDetails?.authorities!!.toTypedArray()[0] as SimpleGrantedAuthority).authority)
    }

    @Test
    @WithCustomUserDetails("user@test.com")
    fun getAuthenticatedUserEmail() {
        assertEquals("user@test.com", AuthenticationHelper.getAuthenticatedUserEmail())
    }

    @Test
    @WithCustomUserDetails("user@test.com")
    fun getAuthenticatedUserAuthorities() {
        val simpleGrantedAuthorities = AuthenticationHelper.getAuthenticatedUserAuthorities()
        assertNotNull(simpleGrantedAuthorities)
        assertEquals(RoleEnum.USER.value, simpleGrantedAuthorities?.get(0)?.authority)
    }

    @Test
    @WithCustomUserDetails("user@test.com")
    fun isAuthenticatedUserInRole() {
        assertTrue(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.USER))
        assertFalse(AuthenticationHelper.isAuthenticatedUserInRole(RoleEnum.ADMIN))
    }

    @Test
    @WithCustomUserDetails("user@test.com")
    fun getAuthenticatedUser() {
        System.out.println(AuthenticationHelper.getAuthenticatedUser())
        assertNotNull(AuthenticationHelper.getAuthenticatedUser())
    }

    /*companion object {
        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            /*val roleUser = Role("token", "USER", 2)
            val user = User(
                    "token",
                    "user@test.com",
                    "Nickname",
                    "Firstname",
                    "Lastname",
                    bCryptPasswordEncoder.encode("password"),
                    roleUser)
            val basicUser = UserDetails(user, listOf(SimpleGrantedAuthority(RoleEnum.USER.value)))

            val roleAdmin = Role("token2", "ADMIN", 1)
            val userAdmin = User(
                    "token2",
                    "admin@test.com",
                    "Nickname",
                    "Firstname",
                    "Lastname",
                    bCryptPasswordEncoder.encode("password"),
                    roleAdmin)
            val adminUser = UserDetails(
                    userAdmin, listOf(SimpleGrantedAuthority(RoleEnum.ADMIN.value)))*/
        }
    }*/
}