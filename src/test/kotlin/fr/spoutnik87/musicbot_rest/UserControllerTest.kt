package fr.spoutnik87.musicbot_rest

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.controller.UserController
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import fr.spoutnik87.musicbot_rest.service.TokenService
import fr.spoutnik87.musicbot_rest.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    UserController::class,
    SpringApplicationContextTestConfig::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfigurationTestConfig::class,
    TokenService::class
])
@WebMvcTest(UserController::class)
class UserControllerTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var roleRepository: RoleRepository

    @MockBean
    private lateinit var serverRepository: ServerRepository

    @MockBean
    private lateinit var groupRepository: GroupRepository

    @MockBean(name = "UUID")
    private lateinit var uuid: UUID

    @MockBean
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Autowired
    private lateinit var securityConfiguration: SecurityConfiguration

    @Test
    fun signUp_ValidParameters_UserCreated() {
        Mockito.`when`(uuid.v4()).thenReturn("token")
        Mockito.`when`(roleRepository.findByName(RoleEnum.USER.value))
                .thenReturn(Role("token", "USER", 2))

        val params = HashMap<String, Any>()
        params["email"] = "test@test.com"
        params["password"] = "password"
        params["nickname"] = "Nickname"
        params["firstname"] = "Firstname"
        params["lastname"] = "Lastname"
        Util.basicTestWithBody(
                mockMvc,
                HttpMethod.POST,
                "/user",
                HashMap(),
                params,
                HttpStatus.CREATED,
                "{\"id\":\"token\",\"email\":\"test@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\"}}")
    }

    @Test
    fun login_ValidParameters_ReturnLoggedUser() {
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

        val params = HashMap<String, Any>()
        params["email"] = "user@test.com"
        params["password"] = "password"
        Util.basicTestWithBody(
                mockMvc,
                HttpMethod.POST,
                "/login",
                HashMap(),
                params,
                HttpStatus.OK,
                "{\"id\":\"token\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\"}}")
    }

    @Test
    fun login_InvalidParameters_ReturnUnauthorizedStatus() {
        val params = HashMap<String, Any>()
        params["email"] = "usera@test.com"
        params["password"] = "password"
        Util.basicTestWithBody(
                mockMvc, HttpMethod.POST, "/login", HashMap(), params, HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun getLogged_ExpiredToken_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val token = JWT.create()
                .withSubject("user@test.com")
                .withExpiresAt(Date(System.currentTimeMillis() - 10))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))

        Util.basicTestWithTokenAndBody(
                mockMvc,
                HttpMethod.GET,
                "/user",
                HashMap(),
                null,
                token,
                HttpStatus.FORBIDDEN)
    }

    @Test
    fun getLogged_InvalidToken_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        Util.basicTestWithTokenAndBody(
                mockMvc,
                HttpMethod.GET,
                "/user",
                HashMap(),
                null,
                "token",
                HttpStatus.FORBIDDEN)
    }

    @Test
    fun getLogged_ValidToken_ReturnLoggedUser() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val token = JWT.create()
                .withSubject("user@test.com")
                .withExpiresAt(Date(System.currentTimeMillis() + 300000))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))

        Util.basicTestWithTokenAndBody(
                mockMvc,
                HttpMethod.GET,
                "/user",
                HashMap(),
                null,
                token,
                HttpStatus.OK,
                "{\"id\":\"basicUserToken\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"userRoleToken\",\"name\":\"USER\"}}")
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getLogged_Authenticated_ReturnLoggedUser() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        Util.basicTest(
                mockMvc,
                HttpMethod.GET,
                "/user",
                HashMap(),
                HttpStatus.OK,
                "{\"id\":\"basicUserToken\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"userRoleToken\",\"name\":\"USER\"}}")
    }

    @Test
    fun getLogged_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/user", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    fun getById_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/user/1", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getById_NotAdmin_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        Util.basicTest(mockMvc, HttpMethod.GET, "/user/1", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = ["ADMIN"])
    fun getById_AdminAndUserNotFound_ReturnNotFoundStatus() {
        Mockito.`when`(userRepository.findByEmail("admin@test.com"))
                .thenReturn(UserFactory().createAdminUser().build())

        Util.basicTest(mockMvc, HttpMethod.GET, "/user/1", HashMap(), HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = ["ADMIN"])
    fun getById_AdminAndUserFound_ReturnUser() {
        Mockito.`when`(userRepository.findByEmail("admin@test.com"))
                .thenReturn(UserFactory().createAdminUser().build())
        Mockito.`when`(userRepository.findByUuid("basicUserToken"))
                .thenReturn(UserFactory().createBasicUser().build())

        Util.basicTest(
                mockMvc,
                HttpMethod.GET,
                "/user/basicUserToken",
                HashMap(),
                HttpStatus.OK,
                "{\"id\":\"basicUserToken\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"userRoleToken\",\"name\":\"USER\",\"lvl\":2}}")
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun update_ChangeUserValues_ReturnNewUser() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        body["firstname"] = "Newfirstname"
        body["lastname"] = "Newlastname"
        Util.basicTestWithBody(
                mockMvc,
                HttpMethod.PUT,
                "/user/basicUserToken",
                HashMap(),
                body,
                HttpStatus.OK,
                "{\"id\":\"basicUserToken\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Newfirstname\",\"lastname\":\"Newlastname\",\"role\":{\"id\":\"userRoleToken\",\"name\":\"USER\"}}")
        Mockito.verify(userRepository, Mockito.atLeastOnce()).save(Mockito.any())
    }
}