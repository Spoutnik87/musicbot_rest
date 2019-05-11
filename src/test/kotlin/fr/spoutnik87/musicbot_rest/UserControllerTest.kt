package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.controller.UserController
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.RoleRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.util.Util
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import fr.spoutnik87.musicbot_rest.util.WithSecurityContextTestExecutionListener
import fr.spoutnik87.musicbot_rest.util.WithUserDetails
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.web.servlet.MockMvc
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    UserController::class
])
@WebMvcTest(UserController::class)
@TestExecutionListeners(listeners = [
    WithSecurityContextTestExecutionListener::class,
    DependencyInjectionTestExecutionListener::class,
    MockitoTestExecutionListener::class
])
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

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun setup() {
        Mockito.`when`(uuid.v4()).thenReturn("token")
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
    fun signUp_ValidParameters_UserCreated() {
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
    @WithUserDetails("user@test.com")
    fun getLogged_Authenticated_ReturnLoggedUser() {
        Util.basicTest(
                mockMvc,
                HttpMethod.GET,
                "/user",
                HashMap(),
                HttpStatus.OK,
                "{\"id\":\"token\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\"}}")
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
    @WithUserDetails("user@test.com")
    fun getById_NotAdmin_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/user/1", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithUserDetails("admin@test.com")
    fun getById_AdminAndUserNotFound_ReturnNotFoundStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/user/1", HashMap(), HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithUserDetails("admin@test.com")
    fun getById_AdminAndUserFound_ReturnUser() {
        Mockito.`when`(userRepository.findByUuid("1"))
                .thenReturn(
                        User(
                                "1",
                                "test@test.com",
                                "Nickname",
                                "Firstname",
                                "Lastname",
                                bCryptPasswordEncoder.encode("password"),
                                Role("token", "USER", 2)))
        Util.basicTest(
                mockMvc,
                HttpMethod.GET,
                "/user/1",
                HashMap(),
                HttpStatus.OK,
                "{\"id\":\"1\",\"email\":\"test@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\",\"lvl\":2}}")
    }

    @Test
    @WithUserDetails("user@test.com")
    fun update_ChangeUserValues_ReturnNewUser() {
        val body = HashMap<String, Any>()
        body["firstname"] = "Newfirstname"
        body["lastname"] = "Newlastname"
        Util.basicTestWithBody(
                mockMvc,
                HttpMethod.PUT,
                "/user/token",
                HashMap(),
                body,
                HttpStatus.OK,
                "{\"id\":\"token\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Newfirstname\",\"lastname\":\"Newlastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\"}}")
        Mockito.verify(userRepository, Mockito.atLeastOnce()).save(Mockito.any())
    }
}