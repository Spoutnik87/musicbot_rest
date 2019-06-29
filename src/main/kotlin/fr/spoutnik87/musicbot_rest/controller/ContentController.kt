package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.ContentCreateReader
import fr.spoutnik87.musicbot_rest.reader.ContentUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.ContentService
import fr.spoutnik87.musicbot_rest.service.FileService
import fr.spoutnik87.musicbot_rest.service.MimeTypeService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.viewmodel.ContentViewModel
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

@RestController
@RequestMapping("content")
class ContentController {

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var contentTypeRepository: ContentTypeRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var contentService: ContentService

    @Autowired
    private lateinit var mimeTypeService: MimeTypeService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/server/{serverId}")
    fun getByServerId(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasReadContentPermission(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(authenticatedUser.getVisibleContents(server).map { ContentViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.OK)
    }

    @GetMapping("/{id}/media")
    fun getMedia(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.hasMedia()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        TODO()
    }

    @GetMapping("/{id}/thumbnail", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getThumbnail(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.contentThumbnailsPath + content.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody contentCreateReader: ContentCreateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val category = categoryRepository.findByUuid(contentCreateReader.categoryId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(category.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val visibleGroupList = contentCreateReader.visibleGroupList.map { Pair(groupRepository.findByUuidAndServer(it.id, category.server) ?: return ResponseEntity(HttpStatus.BAD_REQUEST), it.visible) }.distinctBy { it.first.id }
        val contentType = contentTypeRepository.findByValue(contentCreateReader.contentType)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentService.create(contentCreateReader.name, contentCreateReader.description,
                authenticatedUser, contentType, category) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        visibleGroupList.forEach {
            contentService.setVisible(content, it.first, it.second)
        }
        if (contentCreateReader.link != null && contentType.toEnum == ContentTypeEnum.YOUTUBE) {
            contentService.setYoutubeMetadata(content, contentCreateReader.link)
        }
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(@PathVariable("id") uuid: String, contentUpdateReader: ContentUpdateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        contentUpdateReader.visibleGroupList?.map { Pair(groupRepository.findByUuidAndServer(it.id, content.server) ?: return ResponseEntity(HttpStatus.BAD_REQUEST), it.visible) }?.distinctBy { it.first.id }?.forEach {
            contentService.setVisible(content, it.first, it.second)
        }
        val category = if (contentUpdateReader.categoryId != null) {
            categoryRepository.findByUuid(contentUpdateReader.categoryId)
        } else {
            null
        }
        content = contentService.update(content, contentUpdateReader.name, contentUpdateReader.description, category)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/media")
    fun updateMedia(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.isLocalContent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasCreateContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        content = contentService.updateMedia(content, BufferedInputStream(file.inputStream), file.size) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        content = contentService.updateThumbnail(content, BufferedInputStream(file.inputStream))
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.ACCEPTED)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasDeleteContentPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (content.hasMedia()) {
            fileService.deleteFile(appConfig.contentMediaPath + content.uuid)
        }
        if (content.hasThumbnail()) {
            fileService.deleteFile(appConfig.contentThumbnailsPath + content.uuid)
        }
        contentRepository.delete(content)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}