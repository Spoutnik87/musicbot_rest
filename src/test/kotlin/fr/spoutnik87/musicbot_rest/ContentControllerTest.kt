package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.controller.ContentController
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.FileService
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
    ContentController::class,
    AppConfig::class,
    SpringApplicationContextTestConfig::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class
])
@WebMvcTest(ContentController::class)
@TestExecutionListeners(listeners = [
    WithSecurityContextTestExecutionListener::class,
    DependencyInjectionTestExecutionListener::class,
    MockitoTestExecutionListener::class
])
class ContentControllerTest {

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var contentRepository: ContentRepository

    @MockBean
    private lateinit var groupRepository: GroupRepository

    @MockBean
    private lateinit var contentGroupRepository: ContentGroupRepository

    @MockBean
    private lateinit var contentTypeRepository: ContentTypeRepository

    @MockBean
    private lateinit var categoryRepository: CategoryRepository

    @MockBean
    private lateinit var fileService: FileService

    @MockBean(name = "UUID")
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun setup() {
        Mockito.`when`(uuid.v4()).thenReturn("token")
        /**
         * Default users
         */
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
        mockUserWithWritePermission()
    }

    /**
     * User in group in server with write permission
     */
    fun mockUserWithWritePermission() {
        var user = User("token",
                "userWithWritePermission@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                Role("token", "USER", 2))
        user.id = 1
        var group = Group("groupToken", "Group")
        group.id = 1
        var userGroup = UserGroup("userGroupToken", user, group)
        userGroup.id = 1
        userGroup.permissionSet = HashSet()
        userGroup.permissionSet.add(Permission("permissionToken", PermissionEnum.CREATE_CONTENT.value))
        user.userGroupSet.add(userGroup)
        group.userGroupSet.add(userGroup)
        var server = Server("serverToken", "Server", user)
        server.id = 1
        group.server = server
        server.groupSet.add(group)
        Mockito.`when`(groupRepository.findByUuid("groupToken")).thenReturn(group)
        Mockito.`when`(userRepository.findByEmail("userWithWritePermission@test.com")).thenReturn(user)
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", server))
    }

    @Test
    fun createContent_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun createContent_InvalidParameters_ReturnBadRequestStatus() {
        val body = HashMap<String, Any>()
        body["group"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun createContent_InvalidGroup_ReturnBadRequestStatus() {
        val body = HashMap<String, Any>()
        body["groupId"] = "invalidGroupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithUserDetails("userWithWritePermission@test.com")
    fun createContent_InvalidMediaType_ReturnBadRequestStatus() {
        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithUserDetails("userWithWritePermission@test.com")
    fun createContent_InvalidCategory_ReturnBadRequestStatus() {
        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "invalidCategoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithUserDetails("userWithWritePermission@test.com")
    fun createContent_NoCreatePermission_ReturnForbiddenStatus() {
        Mockito.`when`(groupRepository.findByUuid("groupToken2")).thenReturn(Group("groupToken2", "Group"))
        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken2"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithUserDetails("userWithWritePermission@test.com")
    fun createContent_ValidParameters_ReturnMedia() {
        Mockito.`when`(contentTypeRepository.findByValue(ContentTypeEnum.DEFAULT.value)).thenReturn(ContentType("mediaTypeToken", ContentTypeEnum.DEFAULT.value))
        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.CREATED, "{\"id\":\"token\",\"name\":\"New content\",\"extension\":null,\"size\":null,\"content\":false,\"thumbnail\":false,\"serverId\":\"serverToken\", \"contentType\":{\"id\":\"mediaTypeToken\",\"value\":\"DEFAULT\"},\"category\":{\"id\":\"categoryToken\",\"name\":\"Category\",\"serverId\":\"serverToken\"}}")
        Mockito.verify(contentRepository, Mockito.atLeastOnce()).save(Mockito.any())
    }

    @Test
    fun deleteContent_NotAuthenticated_ReturnForbiddenStatus() {
        Util.basicTest(mockMvc, HttpMethod.DELETE, "/content/mediaToken", HashMap(), HttpStatus.FORBIDDEN)
    }

    @Test
    @WithUserDetails("user@test.com")
    fun deleteContent_InvalidMedia_ReturnBadRequestStatus() {
        Util.basicTest(mockMvc, HttpMethod.DELETE, "/content/invalidMediaToken", HashMap(), HttpStatus.BAD_REQUEST)
    }
}