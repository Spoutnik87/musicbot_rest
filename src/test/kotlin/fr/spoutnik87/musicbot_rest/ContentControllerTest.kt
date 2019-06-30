package fr.spoutnik87.musicbot_rest

import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.controller.ContentController
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.reader.VisibleGroupReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import fr.spoutnik87.musicbot_rest.service.*
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

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [
    ContentController::class,
    UserService::class,
    ContentService::class,
    ContentTypeService::class,
    MimeTypeService::class,
    AppConfig::class,
    SpringApplicationContext::class,
    BCryptPasswordEncoder::class,
    WebSecurityTestConfig::class,
    SecurityConfiguration::class
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
    private lateinit var mimeTypeRepository: MimeTypeRepository

    @MockBean
    private lateinit var serverRepository: ServerRepository

    @MockBean
    private lateinit var fileService: FileService

    @MockBean
    private lateinit var imageService: ImageService

    @MockBean
    private lateinit var youtubeService: YoutubeService

    @MockBean(name = "UUID")
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun createContent_NotAuthenticated_ReturnForbiddenStatus() {
        val body = HashMap<String, Any>()
        body["visibleGroupList"] = listOf(VisibleGroupReader("groupToken", true))
        body["categoryId"] = "categoryToken"
        body["contentType"] = "LOCAL"
        body["name"] = "New content"
        body["description"] = "Desc"
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
        body["contentType"] = "LOCAL"
        body["name"] = "New content"
        body["description"] = "Desc"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidGroup_ReturnBadRequestStatus() {
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(UserFactory().createBasicUser().build())

        val body = HashMap<String, Any>()
        body["visibleGroupList"] = listOf(VisibleGroupReader("invalidGroupToken", true))
        body["categoryId"] = "categoryToken"
        body["contentType"] = "LOCAL"
        body["name"] = "New content"
        body["description"] = "Desc"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidMediaType_ReturnBadRequestStatus() {
        val server = Server("serverToken", "Server", 0)
        val group = GroupFactory().server(server,
                listOf(PermissionFactory().create(PermissionEnum.CREATE_CONTENT).build())
        ).build()
        val user = UserFactory().createBasicUser().inServer(group, server).build()
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(user)
        Mockito.`when`(uuid.v4()).thenReturn("token")
        Mockito.`when`(groupRepository.findByUuidAndServer("groupToken", server)).thenReturn(group)
        Mockito.`when`(contentTypeRepository.findByValue(ContentTypeEnum.LOCAL.value)).thenReturn(ContentType("localContentTypeId", ContentTypeEnum.LOCAL.value))
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", 0, user, server))

        val body = HashMap<String, Any>()
        body["visibleGroupList"] = listOf(VisibleGroupReader("groupToken", true))
        body["categoryId"] = "categoryToken"
        body["contentType"] = "invalidContentType"
        body["name"] = "New content"
        body["description"] = "Desc"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_InvalidCategory_ReturnBadRequestStatus() {
        val server = Server("serverToken", "Server", 0)
        val group = GroupFactory().server(server,
                listOf(PermissionFactory().create(PermissionEnum.CREATE_CONTENT).build())
        ).build()
        val user = UserFactory().createBasicUser().inServer(group, server).build()
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(user)
        Mockito.`when`(groupRepository.findByUuidAndServer("groupToken", server)).thenReturn(group)
        Mockito.`when`(contentTypeRepository.findByValue(ContentTypeEnum.LOCAL.value)).thenReturn(ContentType("localContentTypeId", ContentTypeEnum.LOCAL.value))
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", 0, user, server))

        val body = HashMap<String, Any>()
        body["visibleGroupList"] = listOf(VisibleGroupReader("groupToken", true))
        body["categoryId"] = "invalidCategoryToken"
        body["contentType"] = "LOCAL"
        body["name"] = "New content"
        body["description"] = "Desc"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_NoCreatePermission_ReturnForbiddenStatus() {
        val server = Server("serverToken", "Server", 0)
        val group = GroupFactory().server(server).build()
        val user = UserFactory().createBasicUser().inServer(group, server).build()
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(user)
        Mockito.`when`(groupRepository.findByUuidAndServer("groupToken", server)).thenReturn(group)
        Mockito.`when`(contentTypeRepository.findByValue(ContentTypeEnum.LOCAL.value)).thenReturn(ContentType("localContentTypeId", ContentTypeEnum.LOCAL.value))
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", 0, user, server))
        Mockito.`when`(groupRepository.findByUuid("groupToken2")).thenReturn(GroupFactory().create("groupToken2", "Group").server(server).build())

        val body = HashMap<String, Any>()
        body["visibleGroupList"] = listOf(VisibleGroupReader("groupToken2", true))
        body["categoryId"] = "categoryToken"
        body["contentType"] = "LOCAL"
        body["name"] = "New content"
        body["description"] = "Desc"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.FORBIDDEN)
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = ["USER"])
    fun createContent_ValidParameters_ReturnMedia() {
        val server = Server("serverToken", "Server", 0)
        val group = GroupFactory().server(server,
                listOf(PermissionFactory().create(PermissionEnum.CREATE_CONTENT).build())
        ).build()
        val user = UserFactory().createBasicUser().inServer(group, server).build()
        Mockito.`when`(userRepository.findByEmail("user@test.com"))
                .thenReturn(user)
        Mockito.`when`(groupRepository.findByUuidAndServer("groupToken", server)).thenReturn(group)
        Mockito.`when`(categoryRepository.findByUuid("categoryToken")).thenReturn(Category("categoryToken", "Category", 0, user, server))
        Mockito.`when`(uuid.v4()).thenReturn("token")
        Mockito.`when`(contentTypeRepository.findByValue(ContentTypeEnum.LOCAL.value)).thenReturn(ContentType("localContentTypeId", ContentTypeEnum.LOCAL.value))
        Mockito.`when`(contentRepository.save(ArgumentMatchers.any(Content::class.java))).then { it.getArgument(0) }
        Mockito.`when`(contentGroupRepository.save(ArgumentMatchers.any(ContentGroup::class.java))).then { it.getArgument(0) }
        Mockito.`when`(imageService.generateRandomImage("token")).then { ByteArray(0) }

        val body = HashMap<String, Any>()
        body["visibleGroupList"] = listOf(VisibleGroupReader("groupToken", true))
        body["categoryId"] = "categoryToken"
        body["contentType"] = "LOCAL"
        body["name"] = "New content"
        body["description"] = "Desc"
        Util.basicTestWithBody(mockMvc, HttpMethod.POST, "/content", HashMap(), body, HttpStatus.CREATED, "{\"id\":\"token\",\"createdAt\":0,\"name\":\"New content\",\"description\":\"Desc\",\"thumbnail\":false,\"thumbnailSize\":0,\"duration\":null,\"serverId\":\"serverToken\", " +
                "\"contentType\":{\"id\":\"localContentTypeId\",\"value\":\"LOCAL\"},\"category\":{\"id\":\"categoryToken\",\"name\":\"Category\",\"serverId\":\"serverToken\"},\"groups\":[{\"id\":\"groupToken\",\"name\":\"Group\",\"serverId\":\"serverToken\"}]," +
                "\"localMetadata\": null, \"youtubeMetadata\": null}")
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