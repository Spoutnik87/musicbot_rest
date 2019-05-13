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
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @GetMapping("/guild/{guildId}")
    fun getByGuildId(@PathVariable("guildId") guildId: String): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
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
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
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
    @GetMapping(value = ["/list/{userId}", "/list"])
    fun getByUserId(@PathVariable("userId", required = false) userUuid: String?): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
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
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
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

        var server = Server(uuid.v4(), serverCreateReader.name, authenticatedUser)

        server = serverRepository.save(server)

        var group = Group(uuid.v4(), "Default", server)
        group = groupRepository.save(group)

        var userGroup = UserGroup(authenticatedUser, group)
        val permissionSet = HashSet<Permission>()

        permissionSet.add(createContentPermission)
        permissionSet.add(deleteContentPermission)
        permissionSet.add(readContentPermission)
        permissionSet.add(changeModePermission)
        permissionSet.add(playMediaPermission)
        permissionSet.add(stopMediaPermission)
        permissionSet.add(createCategoryPermission)
        permissionSet.add(deleteCategoryPermission)
        userGroup.permissionSet = permissionSet

        userGroupRepository.save(userGroup)
        return ResponseEntity(ServerViewModel.from(server), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody serverUpdateReader: ServerUpdateReader): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
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
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        serverRepository.delete(server)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}