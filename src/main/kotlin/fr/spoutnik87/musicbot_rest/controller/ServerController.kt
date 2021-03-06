package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.ServerCreateReader
import fr.spoutnik87.musicbot_rest.reader.ServerLinkReader
import fr.spoutnik87.musicbot_rest.reader.ServerUpdateReader
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.service.*
import fr.spoutnik87.musicbot_rest.viewmodel.ServerLinkTokenViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.ServerViewModel
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

@RestController
@RequestMapping("server")
class ServerController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var permissionService: PermissionService

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var serverService: ServerService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var appConfig: AppConfig

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @GetMapping("/guild/{guildId}")
    fun getByGuildId(@PathVariable("guildId") guildId: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.BOT.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val server = serverRepository.findByGuildId(guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/link/{serverId}")
    fun getServerLinkToken(@PathVariable("serverId") serverId: String): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(serverId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        var serverLinkToken = tokenService.createServerLinkToken(server.uuid)
        return ResponseEntity(ServerLinkTokenViewModel(serverLinkToken), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @PostMapping("/link")
    fun linkGuildToServer(@RequestBody serverLinkReader: ServerLinkReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.BOT.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (serverRepository.findByGuildId(serverLinkReader.guildId) != null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val serverLinkToken = tokenService.decodeServerLinkToken(serverLinkReader.token)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(serverLinkToken.id) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        server.guildId = serverLinkReader.guildId
        serverRepository.save(server)
        if (!server.owner.isLinked) {
            server.owner.userId = serverLinkReader.userId
            userRepository.save(server.owner)
        }
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping(value = ["/list/{userId}", "/list"])
    fun getByUserId(@PathVariable("userId", required = false) userUuid: String?): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        // TODO To rafacto
        if (authenticatedUser.uuid == userUuid || userUuid == null) {
            return ResponseEntity(authenticatedUser.serverList.map { ServerViewModel.from(it) }, HttpStatus.OK)
        }
        if (authenticatedUser.role.name != RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val user = userRepository.findByUuid(userUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(user.ownedServerSet.map { ServerViewModel.from(it) }, HttpStatus.OK)
    }

    @GetMapping("/{id}/thumbnail", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getThumbnail(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!server.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.serverThumbnailsPath + server.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody serverCreateReader: ServerCreateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val permissions = permissionService.getDefaultCreateServerPermissions()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverService.create(serverCreateReader.name, authenticatedUser, permissions)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody serverUpdateReader: ServerUpdateReader): ResponseEntity<Any> {
        var server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        server = serverService.update(server, serverUpdateReader.name) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.ACCEPTED)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        server = serverService.updateThumbnail(server, BufferedInputStream(file.inputStream))
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        serverRepository.delete(server)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}