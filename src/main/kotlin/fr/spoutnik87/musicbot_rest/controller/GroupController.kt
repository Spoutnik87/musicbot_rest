package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.GroupCreateReader
import fr.spoutnik87.musicbot_rest.reader.GroupUpdateReader
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.service.FileService
import fr.spoutnik87.musicbot_rest.service.GroupService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.viewmodel.GroupViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.UserGroupViewModel
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

@RestController
@RequestMapping("group")
class GroupController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var appConfig: AppConfig

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val group = groupRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!group.server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(GroupViewModel.from(group), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/server/{id}")
    fun getByServerId(@PathVariable("id") serverUuid: String): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(serverUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(server.groupSet.map { UserGroupViewModel.from(it, authenticatedUser) }, HttpStatus.OK)
    }

    @GetMapping("/{id}/thumbnail", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getThumbnail(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val group = groupRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!group.server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!group.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.groupThumbnailsPath + group.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody groupCreateReader: GroupCreateReader): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(groupCreateReader.serverId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val group = groupService.create(groupCreateReader.name, authenticatedUser, server) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(GroupViewModel.from(group), HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody groupUpdateReader: GroupUpdateReader): ResponseEntity<Any> {
        var group = groupRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(group.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        group = groupService.update(group, groupUpdateReader.name) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(GroupViewModel.from(group), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var group = groupRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(group.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        group = groupService.updateThumbnail(group, BufferedInputStream(file.inputStream))
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(GroupViewModel.from(group), HttpStatus.ACCEPTED)
    }
}