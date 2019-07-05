package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.UserGroup
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.ServerJoinTokenReader
import fr.spoutnik87.musicbot_rest.reader.UserSignupReader
import fr.spoutnik87.musicbot_rest.reader.UserUpdateReader
import fr.spoutnik87.musicbot_rest.repository.*
import fr.spoutnik87.musicbot_rest.service.FileService
import fr.spoutnik87.musicbot_rest.service.PermissionService
import fr.spoutnik87.musicbot_rest.service.TokenService
import fr.spoutnik87.musicbot_rest.service.UserService
import fr.spoutnik87.musicbot_rest.viewmodel.UserServerJoinTokenViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.UserViewModel
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

@RestController
@RequestMapping("user")
class UserController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var permissionService: PermissionService

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var fileService: FileService

    @JsonView(Views.Companion.Mixed::class)
    @GetMapping("")
    fun getLogged(): ResponseEntity<Any> {
        val user = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val user = userRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/server/{serverId}")
    fun getByServerId(@PathVariable("serverId") serverUuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(serverUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(server.userList.map { UserViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/group/{groupId}")
    fun getByGroupId(@PathVariable("groupId") groupUuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val group = groupRepository.findByUuid(groupUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!group.server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(group.userList.map { UserViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/serverJoinToken")
    fun getServerJoinToken(): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        var serverJoinToken = tokenService.createServerJoinToken(authenticatedUser.uuid)
        return ResponseEntity(UserServerJoinTokenViewModel(serverJoinToken), HttpStatus.OK)
    }

    @GetMapping("/{id}/thumbnail", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getThumbnail(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val user = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (user.role.name != RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!user.hasThumbnail()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(IOUtils.toByteArray(fileService.getFile(appConfig.userThumbnailsPath + user.uuid).toURI()), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/joinServer")
    fun joinServer(@RequestBody serverJoinTokenReader: ServerJoinTokenReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.BOT.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val userByUserId = userRepository.findByUserId(serverJoinTokenReader.userId)
        val serverJoinToken = tokenService.decodeServerJoinToken(serverJoinTokenReader.serverJoinToken)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val user = userRepository.findByUuid(serverJoinToken.id) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        /**
         * If the user who sent the join request is already linked to another user.
         */
        if (userByUserId != null && userByUserId.id != user.id) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = serverRepository.findByGuildId(serverJoinTokenReader.guildId)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (server.hasUser(user)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val defaultGroup = server.defaultGroup
        if (defaultGroup.server.hasUser(user)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val userGroup = UserGroup(user, defaultGroup)
        userGroupRepository.save(userGroup)
        if (!user.isLinked) {
            user.userId = serverJoinTokenReader.userId
            userRepository.save(user)
        }
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    @JsonView(Views.Companion.Mixed::class)
    @PostMapping
    fun signup(@RequestBody userSignupReader: UserSignupReader): ResponseEntity<Any> {
        val role = roleRepository.findByName(RoleEnum.USER.value) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val user = userService.create(userSignupReader.email, userSignupReader.nickname, userSignupReader.firstname, userSignupReader.lastname, userSignupReader.password, role)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Mixed::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody userUpdateReader: UserUpdateReader): ResponseEntity<Any> {
        val user = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (user.uuid != uuid) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (userUpdateReader.nickname != null) {
            user.nickname = userUpdateReader.nickname
        }
        if (userUpdateReader.firstname != null) {
            user.firstname = userUpdateReader.firstname
        }
        if (userUpdateReader.lastname != null) {
            user.lastname = userUpdateReader.lastname
        }
        userRepository.save(user)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.OK)
    }

    @PutMapping("/{id}/thumbnail")
    fun updateThumbnail(@PathVariable("id") uuid: String, @RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        var user = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (user.uuid != uuid) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        user = userService.updateThumbnail(user, BufferedInputStream(file.inputStream))
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.ACCEPTED)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        TODO()
    }
}