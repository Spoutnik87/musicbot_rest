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
import fr.spoutnik87.musicbot_rest.service.TokenService
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
    SpringApplicationContextTestConfig::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfigurationTestConfig::class,
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
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CONTENT.value)).thenReturn(Permission("createMediaToken", PermissionEnum.CREATE_CONTENT.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CONTENT.value)).thenReturn(Permission("deleteMediaToken", PermissionEnum.DELETE_CONTENT.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.READ_CONTENT.value)).thenReturn(Permission("readMediaToken", PermissionEnum.READ_CONTENT.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value)).thenReturn(Permission("changeModeToken", PermissionEnum.CHANGE_MODE.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value)).thenReturn(Permission("playMediaToken", PermissionEnum.PLAY_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value)).thenReturn(Permission("stopMediaToken", PermissionEnum.STOP_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value)).thenReturn(Permission("createCategoryToken", PermissionEnum.CREATE_CATEGORY.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)).thenReturn(Permission("deleteCategoryToken", PermissionEnum.DELETE_CATEGORY.value))
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
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server, listOf()).build())
        Mockito.`when`(serverRepository.findByUuid("serverToken")).thenReturn(server)

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
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server, listOf()).build())
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

        val linkServerToken = JWT.create()
                .withClaim("type", "SERVER_LINK_TOKEN")
                .withClaim("id", "serverId")
                .withExpiresAt(Date(System.currentTimeMillis() + 300000))
                .sign(Algorithm.HMAC512(this.securityConfiguration.secret.toByteArray()))

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
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "server")
        server.guildId = "guildId"
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server, listOf()).build())
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
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server/link", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "bot@test.com", authorities = ["BOT"])
    fun linkGuildToServer_ExpiredToken_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("bot@test.com"))
                .thenReturn(UserFactory().createBotUser().build())
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server, listOf()).build())
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
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server, listOf()).build())
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