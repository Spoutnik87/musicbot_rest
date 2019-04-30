package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.GroupCreateReader
import fr.spoutnik87.musicbot_rest.reader.GroupUpdateReader
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    private lateinit var uuid: UUID

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") uuid: String): ResponseEntity<Any> {
        val optionalGroup = groupRepository.findByUuid(uuid)
        if (!optionalGroup.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val group = optionalGroup.get()
        val server = group.server
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = optionalAuthenticatedUser.get()
        return if (!server.hasUser(user)) {
            ResponseEntity(HttpStatus.FORBIDDEN)
        } else ResponseEntity(group, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/server/{id}")
    fun getByServerId(@PathVariable("id") serverUuid: String): ResponseEntity<Any> {
        val optionalServer = serverRepository.findByUuid(serverUuid)
        if (!optionalServer.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = optionalServer.get()
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = optionalAuthenticatedUser.get()
        return if (!server.hasUser(user)) {
            ResponseEntity(HttpStatus.FORBIDDEN)
        } else ResponseEntity(server.groupSet.toTypedArray(), HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("")
    fun create(@RequestBody groupCreateReader: GroupCreateReader): ResponseEntity<Any> {
        val optionalServer = serverRepository.findByUuid(groupCreateReader.serverId)
        if (!optionalServer.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = optionalServer.get()
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = optionalAuthenticatedUser.get()
        if (!user.isOwner(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        val group = Group(uuid.v4(), groupCreateReader.name, server)
        server.groupSet.plus(group)
        groupRepository.save<Group>(group)
        return ResponseEntity<Any>(null!!)
    }

    @JsonView(Views.Companion.Public::class)
    @PutMapping("/{id}")
    fun update(
            @PathVariable("id") uuid: String, @RequestBody groupUpdateReader: GroupUpdateReader): ResponseEntity<Any> {
        val optionalGroup = groupRepository.findByUuid(uuid)
        if (!optionalGroup.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val group = optionalGroup.get()
        val optionalAuthenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
        if (!optionalAuthenticatedUser.isPresent) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!optionalAuthenticatedUser.get().isOwner(group.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        group.name = groupUpdateReader.name
        groupRepository.save(group)
        return ResponseEntity(group, HttpStatus.ACCEPTED)
    }
}