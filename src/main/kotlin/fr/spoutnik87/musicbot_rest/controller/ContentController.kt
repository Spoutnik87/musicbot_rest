package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.model.ContentGroup
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.ContentCreateReader
import fr.spoutnik87.musicbot_rest.reader.ContentUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.FileService
import fr.spoutnik87.musicbot_rest.service.ImageService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.viewmodel.ContentViewModel
import org.apache.commons.io.FilenameUtils
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
    private lateinit var contentGroupRepository: ContentGroupRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var contentTypeRepository: ContentTypeRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var userService: UserService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/server/{serverId}")
    fun getByServerId(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(
                server.contentList.filter { authenticatedUser.hasReadContentPermission(it) }.map { ContentViewModel.from(it) },
                HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getContent(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.OK)
    }

    @GetMapping("/{id}/media")
    fun getMedia(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content)) {
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
        if (!authenticatedUser.hasReadContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.contentThumbnailsPath + content.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun createContent(@RequestBody contentCreateReader: ContentCreateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val group = groupRepository.findByUuid(contentCreateReader.groupId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(group)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        var contentType = contentTypeRepository.findByValue(ContentTypeEnum.DEFAULT.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var category = categoryRepository.findByUuid(contentCreateReader.categoryId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var content = Content(uuid.v4(), contentCreateReader.name, authenticatedUser, contentType, category)
        content = contentRepository.save(content)
        var contentGroup = ContentGroup(content, group)
        contentGroup = contentGroupRepository.save(contentGroup)
        content.contentGroupSet.add(contentGroup)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun updateContent(@PathVariable("id") uuid: String, contentUpdateReader: ContentUpdateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (contentUpdateReader.categoryId != null) {
            val category = categoryRepository.findByUuid(contentUpdateReader.categoryId)
                    ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
            content.category = category
        }
        if (contentUpdateReader.name != null) {
            content.name = contentUpdateReader.name
        }
        contentRepository.save(content)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/media")
    fun updateMedia(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val inputStream = BufferedInputStream(file.inputStream)
        inputStream.mark(file.size.toInt() + 1)
        if (!fileService.isAudio(inputStream)) {
            inputStream.close()
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        inputStream.reset()
        inputStream.mark(file.size.toInt() + 1)
        val duration = fileService.getAudioFileDuration(inputStream)
        if (duration == null) {
            inputStream.close()
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        inputStream.reset()
        if (content.hasMedia()) {
            fileService.deleteFile(appConfig.contentMediaPath + content.uuid)
            content.media = false
            content.extension = null
            content.mediaSize = null
            content.duration = null
            contentRepository.save(content)
        }
        fileService.saveFile(appConfig.contentMediaPath + content.uuid, inputStream.readBytes())
        content.media = true
        content.extension = FilenameUtils.getExtension(file.originalFilename)
        content.mediaSize = file.size
        content.duration = duration
        contentRepository.save(content)
        inputStream.close()
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val inputStream = BufferedInputStream(file.inputStream)
        if (!fileService.isImage(inputStream)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (content.hasThumbnail()) {
            fileService.deleteFile(appConfig.contentThumbnailsPath + content.uuid)
            content.thumbnail = false
            content.thumbnailSize = null
            contentRepository.save(content)
        }
        val resizedThumbnail = try {
            val resized = imageService.resize(inputStream.readBytes(), 200, 200)
            inputStream.close()
            resized
        } catch (e: Exception) {
            inputStream.close()
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        fileService.saveFile(appConfig.contentThumbnailsPath + content.uuid, resizedThumbnail)
        content.thumbnail = true
        content.thumbnailSize = resizedThumbnail.size.toLong()
        contentRepository.save(content)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.ACCEPTED)
    }

    @DeleteMapping("/{id}")
    fun deleteContent(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasDeleteContentPermission(content)) {
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