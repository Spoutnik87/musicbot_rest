package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.ServerCreateReader
import fr.spoutnik87.musicbot_rest.reader.ServerLinkReader
import fr.spoutnik87.musicbot_rest.reader.ServerUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.ServerService
import fr.spoutnik87.musicbot_rest.service.TokenService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.viewmodel.ServerLinkTokenViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.ServerViewModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("server")
class ServerController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var permissionRepository: PermissionRepository

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var serverService: ServerService

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
        if (authenticatedUser.uuid == userUuid || userUuid == null) {
            return ResponseEntity(authenticatedUser.ownedServerSet.map { ServerViewModel.from(it) }, HttpStatus.OK)
        }
        if (authenticatedUser.role.name != RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val user = userRepository.findByUuid(userUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(user.ownedServerSet.map { ServerViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody serverCreateReader: ServerCreateReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val createContentPermission = permissionRepository.findByValue(PermissionEnum.CREATE_CONTENT.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val deleteContentPermission = permissionRepository.findByValue(PermissionEnum.DELETE_CONTENT.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val readContentPermission = permissionRepository.findByValue(PermissionEnum.READ_CONTENT.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val changeModePermission = permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val playMediaPermission = permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val stopMediaPermission = permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val createCategoryPermission = permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val deleteCategoryPermission = permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val permissions = ArrayList<Permission>()
        permissions.add(createContentPermission)
        permissions.add(deleteContentPermission)
        permissions.add(readContentPermission)
        permissions.add(changeModePermission)
        permissions.add(playMediaPermission)
        permissions.add(stopMediaPermission)
        permissions.add(createCategoryPermission)
        permissions.add(deleteCategoryPermission)

        val server = serverService.save(serverCreateReader.name, authenticatedUser, permissions)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody serverUpdateReader: ServerUpdateReader): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        server.name = serverUpdateReader.name
        serverRepository.save(server)
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