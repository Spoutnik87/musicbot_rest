package fr.spoutnik87.musicbot_rest

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.controller.UserController
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import fr.spoutnik87.musicbot_rest.service.PermissionService
import fr.spoutnik87.musicbot_rest.service.TokenService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.util.ServerFactory
import fr.spoutnik87.musicbot_rest.util.UserFactory
import fr.spoutnik87.musicbot_rest.util.Util
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
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
    UserService::class,
    PermissionService::class,
    SpringApplicationContext::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfiguration::class,
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

    @MockBean
    private lateinit var permissionRepository: PermissionRepository

    @Autowired
    private lateinit var tokenService: TokenService

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
        Mockito.`when`(userRepository.save(ArgumentMatchers.any(User::class.java))).then { it.getArgument(0) }

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
                .withExpiresAt(Date(System.currentTimeMillis() - 1000))
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

    @Test
    fun joinServer_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.POST, "/user/joinServer", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun joinServer_BasicUser_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())
        val serverJoinToken = tokenService.createServerJoinToken("basicUserToken")
        val body = HashMap<String, Any>()
        body["guildId"] = "guildId"
        body["userId"] = "userId"
        body["serverJoinToken"] = serverJoinToken
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/user/joinServer", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = ["ADMIN"])
    fun joinServer_AdminUser_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("admin@test.com"))
                .thenReturn(UserFactory().createAdminUser().build())
        val serverJoinToken = tokenService.createServerJoinToken("basicUserToken")
        val body = HashMap<String, Any>()
        body["guildId"] = "guildId"
        body["userId"] = "userId"
        body["serverJoinToken"] = serverJoinToken
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/user/joinServer", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun joinServer_InvalidToken_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByUuid("basicUserToken"))
                .thenReturn(UserFactory().createBasicUser().build())
        val serverJoinToken = tokenService.createServerJoinToken("basicUserToken")
        val body = HashMap<String, Any>()
        body["guildId"] = "guildId"
        body["userId"] = "userId"
        body["serverJoinToken"] = serverJoinToken
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/user/joinServer", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun joinServer_ValidToken_ReturnAcceptedStatus() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())
        val user = UserFactory().createBasicUser().build()
        Mockito.`when`(userRepository.findByUuid("basicUserToken"))
                .thenReturn(user)
        val server = ServerFactory().createDefault()
                .owner(user)
                .link("guildId")
                .defaultGroup("groupToken", "groupName")
                .build()
        Mockito.`when`(serverRepository.findByGuildId("guildId"))
                .thenReturn(server)
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CONTENT.value)).thenReturn(Permission("createMediaToken", PermissionEnum.CREATE_CONTENT.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CONTENT.value)).thenReturn(Permission("deleteMediaToken", PermissionEnum.DELETE_CONTENT.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.READ_CONTENT.value)).thenReturn(Permission("readMediaToken", PermissionEnum.READ_CONTENT.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value)).thenReturn(Permission("changeModeToken", PermissionEnum.CHANGE_MODE.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value)).thenReturn(Permission("playMediaToken", PermissionEnum.PLAY_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value)).thenReturn(Permission("stopMediaToken", PermissionEnum.STOP_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.PAUSE_MEDIA.value)).thenReturn(Permission("pauseMediaToken", PermissionEnum.PAUSE_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.RESUME_MEDIA.value)).thenReturn(Permission("resumeMediaToken", PermissionEnum.RESUME_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.UPDATE_POSITION_MEDIA.value)).thenReturn(Permission("updatePositionMediaToken", PermissionEnum.UPDATE_POSITION_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CLEAR_QUEUE.value)).thenReturn(Permission("clearQueueToken", PermissionEnum.CLEAR_QUEUE.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value)).thenReturn(Permission("createCategoryToken", PermissionEnum.CREATE_CATEGORY.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)).thenReturn(Permission("deleteCategoryToken", PermissionEnum.DELETE_CATEGORY.value))
        val serverJoinToken = tokenService.createServerJoinToken("basicUserToken")
        val body = HashMap<String, Any>()
        body["guildId"] = "guildId"
        body["userId"] = "userId"
        body["serverJoinToken"] = serverJoinToken
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/user/joinServer", HashMap(), body, HttpStatus.ACCEPTED)
    }
}