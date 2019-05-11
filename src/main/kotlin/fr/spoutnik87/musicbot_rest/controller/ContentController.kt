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
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.viewmodel.ContentViewModel
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var fileService: FileService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getContent(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.OK)
    }

    @GetMapping("/{id}/content")
    fun getMedia(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.hasContent()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        TODO()
    }

    @GetMapping("/{id}/thumbnail", produces = [MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE])
    fun getThumbnail(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.applicationPath + THUMBNAILS_PATH + content.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun createContent(@RequestBody contentCreateReader: ContentCreateReader): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val group = groupRepository.findByUuid(contentCreateReader.groupId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(group)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        var contentType = contentTypeRepository.findByValue(ContentTypeEnum.DEFAULT.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var category = categoryRepository.findByUuid(contentCreateReader.categoryId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = Content(uuid.v4(), contentCreateReader.name, contentType, category)
        val contentGroup = ContentGroup(uuid.v4(), content, group)
        content.contentGroupSet.add(contentGroup)
        group.contentGroupSet.add(contentGroup)
        contentRepository.save(content)
        contentGroupRepository.save(contentGroup)
        groupRepository.save(group)
        return ResponseEntity(ContentViewModel.from(content), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun updateMedia(@PathVariable("id") uuid: String, contentUpdateReader: ContentUpdateReader): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
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

    @PutMapping("/{id}/content")
    fun updateMedia(@PathVariable("id") uuid: String, @RequestParam("content") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (content.hasContent()) {
            fileService.deleteFile(appConfig.applicationPath + CONTENTS_PATH + content.uuid)
            content.content = false
            content.extension = null
            content.size = null
            contentRepository.save(content)
        }
        fileService.saveFile(appConfig.applicationPath + CONTENTS_PATH + content.uuid, file.bytes)
        content.content = true
        content.extension = FilenameUtils.getExtension(file.originalFilename)
        content.size = file.size
        contentRepository.save(content)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("thumbnail") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (content.hasThumbnail()) {
            fileService.deleteFile(appConfig.applicationPath + THUMBNAILS_PATH + content.uuid)
            content.thumbnail = false
            contentRepository.save(content)
        }
        fileService.saveFile(appConfig.applicationPath + THUMBNAILS_PATH + content.uuid, file.bytes)
        content.thumbnail = true
        contentRepository.save(content)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @DeleteMapping("/{id}")
    fun deleteContent(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasDeleteContentPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (content.hasContent()) {
            fileService.deleteFile(appConfig.applicationPath + CONTENTS_PATH + content.uuid)
        }
        if (content.hasThumbnail()) {
            fileService.deleteFile(appConfig.applicationPath + THUMBNAILS_PATH + content.uuid)
        }
        contentRepository.delete(content)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    companion object {
        const val CONTENTS_PATH = "contents/content/"
        const val THUMBNAILS_PATH = "contents/thumbnails/"
    }
}