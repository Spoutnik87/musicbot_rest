package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.UserSignupReader
import fr.spoutnik87.musicbot_rest.reader.UserUpdateReader
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.RoleRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.viewmodel.UserViewModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*

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
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @JsonView(Views.Companion.Mixed::class)
    @GetMapping("")
    fun getLogged(): ResponseEntity<Any> {
        val user = AuthenticationHelper.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Private::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (authenticatedUser.role.name != RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val user = userRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/server/{serverId}")
    fun getByServerId(@PathVariable("serverId") serverUuid: String): ResponseEntity<Any> {
        val server = serverRepository.findByUuid(serverUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(server.userList.map { UserViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/group/{groupId}")
    fun getByGroupId(@PathVariable("groupId") groupUuid: String): ResponseEntity<Any> {
        val group = groupRepository.findByUuid(groupUuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val authenticatedUser = AuthenticationHelper.getAuthenticatedUser()
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!group.server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        return ResponseEntity(group.userList.map { UserViewModel.from(it) }, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Mixed::class)
    @PostMapping
    fun signup(@RequestBody userSignupReader: UserSignupReader): ResponseEntity<Any> {
        val role = roleRepository.findByName(RoleEnum.USER.value) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val user = User(
                uuid.v4(),
                userSignupReader.email,
                userSignupReader.nickname,
                userSignupReader.firstname,
                userSignupReader.lastname,
                bCryptPasswordEncoder.encode(userSignupReader.password),
                role)
        this.userRepository.save(user)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Mixed::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody userUpdateReader: UserUpdateReader): ResponseEntity<Any> {
        val user = AuthenticationHelper.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (user.uuid != uuid) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (userUpdateReader.nickname != null) {
            user.nickname = userUpdateReader.nickname!!
        }
        if (userUpdateReader.firstname != null) {
            user.firstname = userUpdateReader.firstname!!
        }
        if (userUpdateReader.lastname != null) {
            user.lastname = userUpdateReader.lastname!!
        }
        userRepository.save(user)
        return ResponseEntity(UserViewModel.from(user), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") uuid: String) {
        TODO()
    }
}