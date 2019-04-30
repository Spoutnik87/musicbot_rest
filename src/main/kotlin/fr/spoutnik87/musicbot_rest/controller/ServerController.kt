package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.reader.ServerCreateReader
import fr.spoutnik87.musicbot_rest.reader.ServerUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
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
    private lateinit var botRepository: BotRepository

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
        val optionalServer = serverRepository.findByUuid(uuid)
        return if (!optionalServer.isPresent) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(optionalServer.get(), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/{userId}")
    fun getByUserId(@PathVariable("userId") userUuid: String): ResponseEntity<Any> {
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        var user = optionalAuthenticatedUser.get()
        if (user.uuid === userUuid) {
            return ResponseEntity(user.serverSet.toTypedArray(), HttpStatus.OK)
        }
        if (user.role.name !== RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val optionalUser = userRepository.findByUuid(userUuid)
        if (!optionalUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        user = optionalUser.get()
        return ResponseEntity(user.serverSet.toTypedArray(), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody serverCreateReader: ServerCreateReader): ResponseEntity<Any> {
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = optionalAuthenticatedUser.get()
        val server = Server(uuid.v4(), serverCreateReader.name, user)
        user.serverSet.plus(server)
        val bot = Bot(uuid.v4(), "Bot " + serverCreateReader.name, serverCreateReader.token)
        server.bot = bot

        val group = Group(uuid.v4(), "Default", server)
        val userGroup = UserGroup(uuid.v4(), user, group)
        val permissionSet = HashSet<Permission>()
        permissionSet.add(
                permissionRepository.findByValue(PermissionEnum.CREATE_MEDIA.value).get())
        permissionSet.add(
                permissionRepository.findByValue(PermissionEnum.DELETE_MEDIA.value).get())
        permissionSet.add(permissionRepository.findByValue(PermissionEnum.READ_MEDIA.value).get())
        permissionSet.add(
                permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value).get())
        permissionSet.add(permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value).get())
        permissionSet.add(permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value).get())
        permissionSet.add(
                permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value).get())
        permissionSet.add(
                permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value).get())
        userGroup.permissionSet = permissionSet
        user.userGroupSet.plus(userGroup)
        group.userGroupSet.plus(userGroup)

        serverRepository.save(server)
        botRepository.save(bot)
        groupRepository.save(group)
        userGroupRepository.save(userGroup)
        userRepository.save(user)
        return ResponseEntity(server, HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody serverUpdateReader: ServerUpdateReader): ResponseEntity<Any> {
        val optionalServer = serverRepository.findByUuid(uuid)
        if (!optionalServer.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = optionalServer.get()
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        val user = optionalAuthenticatedUser.get()
        if (!user.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        server.name = serverUpdateReader.name
        serverRepository.save(server)
        return ResponseEntity(server, HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Public::class)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val optionalServer = serverRepository.findByUuid(uuid)
        if (!optionalServer.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = optionalServer.get()
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        val user = optionalAuthenticatedUser.get()
        if (!user.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        serverRepository.delete(server)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}