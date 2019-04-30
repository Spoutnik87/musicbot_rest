package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.controller.ServerController
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.util.SpringApplicationContextTestConfig
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    ServerController::class,
    SpringApplicationContextTestConfig::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class
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
    private lateinit var botRepository: BotRepository

    @MockBean
    private lateinit var userGroupRepository: UserGroupRepository

    @MockBean
    private lateinit var permissionRepository: PermissionRepository
}