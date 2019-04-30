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

    @JsonView(Views.Companion.Public::class)
    @GetMapping("")
    fun getLogged(): ResponseEntity<Any> {
        val optionalUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        return if (!optionalUser.isPresent) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(optionalUser.get(), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (optionalAuthenticatedUser.get().role.name !== RoleEnum.ADMIN.value) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val optionalUser = userRepository.findByUuid(uuid)
        return if (!optionalUser.isPresent) {
            ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(optionalUser.get(), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/server/{serverId}")
    fun getByServerId(@PathVariable("serverId") serverUuId: String): ResponseEntity<Any> {
        val optionalServer = serverRepository.findByUuid(serverUuId)
        if (!optionalServer.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = optionalServer.get()
        return if (!server.hasUser(optionalAuthenticatedUser.get())) {
            ResponseEntity(HttpStatus.FORBIDDEN)
        } else ResponseEntity(server.userList, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/list/group/{groupId}")
    fun getByGroupId(@PathVariable("groupId") groupUuid: String): ResponseEntity<Any> {
        val optionalGroup = groupRepository.findByUuid(groupUuid)
        if (!optionalGroup.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val group = optionalGroup.get()
        val server = group.server
        return if (!server.hasUser(optionalAuthenticatedUser.get())) {
            ResponseEntity(HttpStatus.FORBIDDEN)
        } else ResponseEntity(group.userList, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping
    fun signup(@RequestBody userSignupReader: UserSignupReader): ResponseEntity<Any> {
        val optionalUserRole = roleRepository.findByName(RoleEnum.USER.value!!)
        if (!optionalUserRole.isPresent()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = User(
                uuid.v4(),
                userSignupReader.email,
                userSignupReader.nickname,
                userSignupReader.firstname,
                userSignupReader.lastname,
                bCryptPasswordEncoder.encode(userSignupReader.password),
                optionalUserRole.get())
        this.userRepository.save(user)
        return ResponseEntity(user, HttpStatus.CREATED)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody userUpdateReader: UserUpdateReader): ResponseEntity<Any> {
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = optionalAuthenticatedUser.get()
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
        return ResponseEntity(user, HttpStatus.OK)
    }
}