package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.controller.ServerController
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.util.*
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

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    ServerController::class,
    SpringApplicationContextTestConfig::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class
])
@WebMvcTest(ServerController::class)
@TestExecutionListeners(listeners = [
    WithSecurityContextTestExecutionListener::class,
    DependencyInjectionTestExecutionListener::class,
    MockitoTestExecutionListener::class
])
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
    fun create_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun create_InvalidParameters_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/server", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun create_ValidParameters_ReturnServer() {
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_MEDIA.value)).thenReturn(Permission("createMediaToken", PermissionEnum.CREATE_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_MEDIA.value)).thenReturn(Permission("deleteMediaToken", PermissionEnum.DELETE_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.READ_MEDIA.value)).thenReturn(Permission("readMediaToken", PermissionEnum.READ_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value)).thenReturn(Permission("changeModeToken", PermissionEnum.CHANGE_MODE.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value)).thenReturn(Permission("playMediaToken", PermissionEnum.PLAY_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value)).thenReturn(Permission("stopMediaToken", PermissionEnum.STOP_MEDIA.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value)).thenReturn(Permission("createCategoryToken", PermissionEnum.CREATE_CATEGORY.value))
        Mockito.`when`(permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)).thenReturn(Permission("deleteCategoryToken", PermissionEnum.DELETE_CATEGORY.value))

        val body = HashMap<String, Any>()
        body["name"] = "New server"
        Util.basicPrintWithBody(mockMvc, HttpMethod.POST, "/server", HashMap(), body)
    }
}