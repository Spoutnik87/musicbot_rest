package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.reader.ServerCreateReader
import fr.spoutnik87.musicbot_rest.reader.ServerLinkReader
import fr.spoutnik87.musicbot_rest.reader.ServerUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
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

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(server, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @GetMapping("/guild/{guildId}")
    fun getByGuildId(@PathVariable("guildId") guildId: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.BOT.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val server = serverRepository.findByGuildId(guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @PostMapping("/link/{serverId}")
    fun linkGuildToServer(@PathVariable("serverId") serverId: String, @RequestBody serverLinkReader: ServerLinkReader): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.BOT.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val server = serverRepository.findByUuid(serverId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (server.isLinked || server.linkToken != serverLinkReader.token) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        server.guildId = serverLinkReader.guildId
        server.linkToken = null
        serverRepository.save(server)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/{userId}")
    fun getByUserId(@PathVariable("userId") userUuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.uuid == userUuid) {
            return ResponseEntity(authenticatedUser.serverSet.toTypedArray(), HttpStatus.OK)
        }
        if (authenticatedUser.role.name != RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val user = userRepository.findByUuid(userUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(user.serverSet.toTypedArray().map { ServerViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody serverCreateReader: ServerCreateReader): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = Server(uuid.v4(), serverCreateReader.name, authenticatedUser)
        authenticatedUser.serverSet.add(server)

        val group = Group(uuid.v4(), "Default", server)
        val userGroup = UserGroup(uuid.v4(), authenticatedUser, group)
        val permissionSet = HashSet<Permission>()

        val createMediaPermission = permissionRepository.findByValue(PermissionEnum.CREATE_MEDIA.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val deleteMediaPermission = permissionRepository.findByValue(PermissionEnum.DELETE_MEDIA.value)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val readMediaPermission = permissionRepository.findByValue(PermissionEnum.READ_MEDIA.value)
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

        permissionSet.add(createMediaPermission)
        permissionSet.add(deleteMediaPermission)
        permissionSet.add(readMediaPermission)
        permissionSet.add(changeModePermission)
        permissionSet.add(playMediaPermission)
        permissionSet.add(stopMediaPermission)
        permissionSet.add(createCategoryPermission)
        permissionSet.add(deleteCategoryPermission)
        userGroup.permissionSet = permissionSet
        authenticatedUser.userGroupSet.add(userGroup)
        group.userGroupSet.add(userGroup)

        serverRepository.save(server)
        groupRepository.save(group)
        userGroupRepository.save(userGroup)
        userRepository.save(authenticatedUser)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody serverUpdateReader: ServerUpdateReader): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
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
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        serverRepository.delete(server)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}