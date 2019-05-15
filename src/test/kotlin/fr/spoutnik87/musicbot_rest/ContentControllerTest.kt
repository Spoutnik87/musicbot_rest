package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.controller.ContentController
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.FileService
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

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    ContentController::class,
    AppConfig::class,
    SpringApplicationContextTestConfig::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfigurationTestConfig::class
])
@WebMvcTest(ContentController::class)
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

    @Test
    fun createContent_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidParameters_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        body["group"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidGroup_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        body["groupId"] = "invalidGroupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidMediaType_ReturnBadRequestStatus() {
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "Server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server,
                        listOf(Permission("permissionToken", PermissionEnum.CREATE_CONTENT.value))).build())
        Mockito.`when`(groupRepository.findByUuid("groupToken")).thenReturn(group)
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", server))

        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidCategory_ReturnBadRequestStatus() {
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "Server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server,
                        listOf(Permission("permissionToken", PermissionEnum.CREATE_CONTENT.value))).build())
        Mockito.`when`(groupRepository.findByUuid("groupToken")).thenReturn(group)
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", server))

        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken"
        body["categoryId"] = "invalidCategoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_NoCreatePermission_ReturnForbiddenStatus() {
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "Server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server,
                        listOf(Permission("permissionToken", PermissionEnum.CREATE_CONTENT.value))).build())
        Mockito.`when`(groupRepository.findByUuid("groupToken")).thenReturn(group)
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", server))
        Mockito.`when`(groupRepository.findByUuid("groupToken2")).thenReturn(Group("groupToken2", "Group"))

        val body = HashMap<String, Any>()
        body["groupId"] = "groupToken2"
        body["categoryId"] = "categoryToken"
        body["name"] = "New content"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_ValidParameters_ReturnMedia() {
        val group = Group("groupToken", "Group")
        val server = Server("serverToken", "Server")
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().inServer(group, server,
                        listOf(Permission("permissionToken", PermissionEnum.CREATE_CONTENT.value))).build())
        Mockito.`when`(groupRepository.findByUuid("groupToken")).thenReturn(group)
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", server))
        Mockito.`when`(uuid.v4()).thenReturn("token")
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
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun deleteContent_InvalidMedia_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        Util.basicTest(mockMvc, HttpMethod.DELETE, "/content/invalidMediaToken", HashMap(), HttpStatus.BAD_REQUEST)
    }
}