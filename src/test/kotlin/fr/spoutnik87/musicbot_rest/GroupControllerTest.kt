package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.controller.GroupController
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.PermissionRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import fr.spoutnik87.musicbot_rest.service.*
import fr.spoutnik87.musicbot_rest.util.Util
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    GroupController::class,
    UserService::class,
    PermissionService::class,
    GroupService::class,
    AppConfig::class,
    SpringApplicationContext::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfiguration::class
])
@WebMvcTest(GroupController::class)
class GroupControllerTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var groupRepository: GroupRepository

    @MockBean
    private lateinit var serverRepository: ServerRepository

    @MockBean
    private lateinit var permissionRepository: PermissionRepository

    @MockBean
    private lateinit var fileService: FileService

    @MockBean
    private lateinit var imageService: ImageService

    @MockBean(name = "UUID")
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Test
    fun create_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["serverId"] = "serverToken"
        body["name"] = "New group"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/group", HashMap(), body, HttpStatus.FORBIDDEN)
    }
}