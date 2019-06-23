package fr.spoutnik87.musicbot_rest

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.controller.ServerController
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import fr.spoutnik87.musicbot_rest.service.PermissionService
import fr.spoutnik87.musicbot_rest.service.ServerService
import fr.spoutnik87.musicbot_rest.service.TokenService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.util.*
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
import kotlin.collections.HashMap

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    ServerController::class,
    UserService::class,
    ServerService::class,
    PermissionService::class,
    SpringApplicationContext::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfiguration::class,
    TokenService::class
])
@WebMvcTest(ServerController::class)
class ServerControllerTest {
    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var groupRepository: GroupRepository

    @MockBean
    private lateinit var serverRepository: ServerRepository

    @MockBean
    private lateinit var userGroupRepository: UserGroupRepository

    @MockBean
    private lateinit var permissionRepository: PermissionRepository

    @Autowired
    private lateinit var tokenService: TokenService

    @MockBean(name = "UUID")
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Autowired
    private lateinit var securityConfiguration: SecurityConfiguration

    @Test
    fun create_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun create_InvalidParameters_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun create_ValidParameters_ReturnServer() {
        Mockito.`when`(uuid.v4()).thenReturn("token")
        Mockito.`when`(userRepository.findByEmail("user@test.com")).thenReturn(UserFactory().createBasicUser().build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CONTENT.value)).thenReturn(PermissionFactory().create(PermissionEnum.CREATE_CONTENT).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CONTENT.value)).thenReturn(PermissionFactory().create(PermissionEnum.DELETE_CONTENT).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.READ_CONTENT.value)).thenReturn(PermissionFactory().create(PermissionEnum.READ_CONTENT).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value)).thenReturn(PermissionFactory().create(PermissionEnum.CHANGE_MODE).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value)).thenReturn(PermissionFactory().create(PermissionEnum.PLAY_MEDIA).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value)).thenReturn(PermissionFactory().create(PermissionEnum.STOP_MEDIA).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.PAUSE_MEDIA.value)).thenReturn(PermissionFactory().create(PermissionEnum.PAUSE_MEDIA).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.RESUME_MEDIA.value)).thenReturn(PermissionFactory().create(PermissionEnum.RESUME_MEDIA).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.UPDATE_POSITION_MEDIA.value)).thenReturn(PermissionFactory().create(PermissionEnum.UPDATE_POSITION_MEDIA).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CLEAR_QUEUE.value)).thenReturn(PermissionFactory().create(PermissionEnum.CLEAR_QUEUE).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value)).thenReturn(PermissionFactory().create(PermissionEnum.CREATE_CATEGORY).build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)).thenReturn(PermissionFactory().create(PermissionEnum.DELETE_CATEGORY).build())
        Mockito.`when`(serverRepository.save(ArgumentMatchers.any(Server::class.java))).then { it.getArgument(0) }
        Mockito.`when`(groupRepository.save(ArgumentMatchers.any(Group::class.java))).then { it.getArgument(0) }

        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server", HashMap(), body, HttpStatus.CREATED, "{\"id\":\"token\",\"name\":\"New server\",\"ownerId\":\"basicUserToken\",\"linked\": false}")
        Mockito.verify(serverRepository, Mockito.atLeastOnce()).save(Mockito.any())
    }

    @Test
    fun update_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicTestWithBody(mockMvc, HttpMethod.PUT, "/server/serverToken", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun update_InvalidServer_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicTestWithBody(mockMvc, HttpMethod.PUT, "/server/serverToken", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun update_ValidParameters_ReturnServer() {
        val group = Group("groupToken", "Group", 0)
        val server = Server("serverToken", "server", 0)
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server).build())
        Mockito.`when`(serverRepository.findByUuid("serverToken")).thenReturn(server)
        Mockito.`when`(serverRepository.save(ArgumentMatchers.any(Server::class.java))).then { it.getArgument(0) }

        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicTestWithBody(mockMvc, HttpMethod.PUT, "/server/serverToken", HashMap(), body, HttpStatus.ACCEPTED, "{\"id\":\"serverToken\",\"name\":\"New server\",\"ownerId\":\"basicUserToken\",\"linked\":false}")
    }

    @Test
    fun delete_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        Util.basicTestWithBody(mockMvc, HttpMethod.DELETE, "/server/serverToken", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    fun getByGuildId_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/server/guild/guildId", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getByGuildId_BasicUser_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        Util.basicTest(mockMvc, HttpMethod.GET, "/server/guild/guildId", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = ["ADMIN"])
    fun getByGuildId_AdminUser_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("admin@test.com"))
                .thenReturn(UserFactory().createAdminUser().build())

        Util.basicTest(mockMvc, HttpMethod.GET, "/server/guild/guildId", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun getByGuildId_BotUserAndServerExists_ReturnServer() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())
        Mockito.`when`(serverRepository.findByGuildId("guildId"))
                .thenReturn(ServerFactory().create("serverToken", "Server", "guildId").build())

        Util.basicTest(mockMvc, HttpMethod.GET, "/server/guild/guildId", HashMap(), HttpStatus.OK, "{\"id\":\"serverToken\",\"name\":\"Server\",\"ownerId\":\"basicUserToken\",\"linked\":true}")
    }

    @Test
    fun getById_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/server/serverId", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun getById_ValidParameters_ReturnServer() {
        val group = Group("groupToken", "Group", 0)
        val server = Server("serverToken", "server", 0)
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server).build())
        Mockito.`when`(serverRepository.findByUuid("serverToken")).thenReturn(server)

        Util.basicTest(mockMvc, HttpMethod.GET, "/server/serverToken", HashMap(), HttpStatus.OK, "{\"id\":\"serverToken\",\"name\":\"server\",\"ownerId\":\"basicUserToken\",\"linked\":false}")
    }

    @Test
    fun linkGuildToServer_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.GET, "/server/link/serverToken", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun linkGuildToServer_BasicUser_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        body["userId"] = "userId"
        body["token"] = "linkToken"
        body["guildId"] = "guildId"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = ["ADMIN"])
    fun linkGuildToServer_AdminUser_ReturnForbiddenStatus() {
        Mockito.`when`(userRepository.findByEmail("admin@test.com"))
                .thenReturn(UserFactory().createAdminUser().build())

        val body = HashMap<String, Any>()
        body["userId"] = "userId"
        body["token"] = "linkToken"
        body["guildId"] = "guildId"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun linkGuildToServer_ServerNotFound_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())

        val linkServerToken = tokenService.createServerLinkToken("serverId")

        val body = HashMap<String, Any>()
        body["userId"] = "userId"
        body["token"] = linkServerToken
        body["guildId"] = "guildId"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun linkGuildToServer_ServerAlreadyLinked_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())
        val group = Group("groupToken", "Group", 0)
        val server = Server("serverToken", "server", 0)
        server.guildId = "guildId"
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server).build())
        Mockito.`when`(serverRepository.findByUuid("serverToken")).thenReturn(server)

        val linkServerToken = tokenService.createServerLinkToken("serverToken")

        val body = HashMap<String, Any>()
        body["userId"] = "userId"
        body["token"] = linkServerToken
        body["guildId"] = "guildId"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun linkGuildToServer_ExpiredToken_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())
        val group = Group("groupToken", "Group", 0)
        val server = Server("serverToken", "server", 0)
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server).build())
        Mockito.`when`(serverRepository.findByUuid("serverToken")).thenReturn(server)

        val linkServerToken = JWT.create()
                .withClaim("type", "SERVER_LINK_TOKEN")
                .withClaim("id", "serverToken")
                .withExpiresAt(Date(System.currentTimeMillis() - 10))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))

        val body = HashMap<String, Any>()
        body["userId"] = "userId"
        body["token"] = linkServerToken
        body["guildId"] = "guildId"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun linkGuildToServer_ValidParameters_ReturnServer() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())
        val group = Group("groupToken", "Group", 0)
        val server = Server("serverToken", "server", 0)
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server).build())
        Mockito.`when`(serverRepository.findByUuid("serverToken")).thenReturn(server)

        val linkServerToken = JWT.create()
                .withClaim("type", "SERVER_LINK_TOKEN")
                .withClaim("id", "serverToken")
                .withExpiresAt(Date(System.currentTimeMillis() + 300000))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))

        val body = HashMap<String, Any>()
        body["userId"] = "userId"
        body["token"] = linkServerToken
        body["guildId"] = "guildId"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.ACCEPTED, "{\"id\":\"serverToken\",\"name\":\"server\",\"ownerId\":\"basicUserToken\",\"linked\":true}")
    }
}