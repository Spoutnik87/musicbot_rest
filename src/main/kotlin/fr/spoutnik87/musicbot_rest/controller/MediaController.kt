package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.MediaTypeEnum
import fr.spoutnik87.musicbot_rest.model.Media
import fr.spoutnik87.musicbot_rest.model.MediaGroup
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.MediaCreateReader
import fr.spoutnik87.musicbot_rest.reader.MediaUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.FileService
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.viewmodel.MediaViewModel
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("media")
class MediaController {

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var mediaRepository: MediaRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var mediaGroupRepository: MediaGroupRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var mediaTypeRepository: MediaTypeRepository

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var fileService: FileService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getMedia(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(MediaViewModel.from(media), HttpStatus.OK)
    }

    @GetMapping("/{id}/content")
    fun getContent(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!media.hasContent()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        TODO()
    }

    @GetMapping("/{id}/thumbnail", produces = [MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE])
    fun getThumbnail(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasReadMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!media.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.applicationPath + THUMBNAILS_PATH + media.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun createMedia(@RequestBody mediaCreateReader: MediaCreateReader): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val group = groupRepository.findByUuid(mediaCreateReader.groupId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateMediaPermission(group)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        var mediaType = mediaTypeRepository.findByValue(MediaTypeEnum.DEFAULT.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var category = categoryRepository.findByUuid(mediaCreateReader.categoryId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = Media(uuid.v4(), mediaCreateReader.name, mediaType, category)
        val mediaGroup = MediaGroup(uuid.v4(), media, group)
        media.mediaGroupSet.add(mediaGroup)
        group.mediaGroupSet.add(mediaGroup)
        mediaRepository.save(media)
        mediaGroupRepository.save(mediaGroup)
        groupRepository.save(group)
        return ResponseEntity(MediaViewModel.from(media), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun updateMedia(@PathVariable("id") uuid: String, mediaUpdateReader: MediaUpdateReader): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (mediaUpdateReader.categoryId != null) {
            val category = categoryRepository.findByUuid(mediaUpdateReader.categoryId)
                    ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
            media.category = category
        }
        if (mediaUpdateReader.name != null) {
            media.name = mediaUpdateReader.name
        }
        mediaRepository.save(media)
        return ResponseEntity(MediaViewModel.from(media), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/content")
    fun updateContent(@PathVariable("id") uuid: String, @RequestParam("content") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (media.hasContent()) {
            fileService.deleteFile(appConfig.applicationPath + CONTENTS_PATH + media.uuid)
            media.content = false
            media.extension = null
            media.size = null
            mediaRepository.save(media)
        }
        fileService.saveFile(appConfig.applicationPath + CONTENTS_PATH + media.uuid, file.bytes)
        media.content = true
        media.extension = FilenameUtils.getExtension(file.originalFilename)
        media.size = file.size
        mediaRepository.save(media)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("thumbnail") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasCreateMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (media.hasThumbnail()) {
            fileService.deleteFile(appConfig.applicationPath + THUMBNAILS_PATH + media.uuid)
            media.thumbnail = false
            mediaRepository.save(media)
        }
        fileService.saveFile(appConfig.applicationPath + THUMBNAILS_PATH + media.uuid, file.bytes)
        media.thumbnail = true
        mediaRepository.save(media)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @DeleteMapping("/{id}")
    fun deleteMedia(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val media = mediaRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasDeleteMediaPermission(media)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (media.hasContent()) {
            fileService.deleteFile(appConfig.applicationPath + CONTENTS_PATH + media.uuid)
        }
        if (media.hasThumbnail()) {
            fileService.deleteFile(appConfig.applicationPath + THUMBNAILS_PATH + media.uuid)
        }
        mediaRepository.delete(media)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    companion object {
        const val CONTENTS_PATH = "media/contents/"
        const val THUMBNAILS_PATH = "media/thumbnails/"
    }
}