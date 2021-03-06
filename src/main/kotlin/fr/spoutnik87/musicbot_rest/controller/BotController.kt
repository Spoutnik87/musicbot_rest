package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.BotPositionReader
import fr.spoutnik87.musicbot_rest.reader.BotStopContentReader
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.service.BotService
import fr.spoutnik87.musicbot_rest.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bot")
class BotController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var botService: BotService

    @Autowired
    private lateinit var userService: UserService

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{serverId}")
    fun getStatus(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.getServerStatus(server.guildId!!) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/play/{contentId}")
    fun play(@PathVariable("contentId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.isPlayable()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasPlayMediaPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.playContent(content.server.guildId!!, content.uuid, authenticatedUser.userId!!, content.youtubeMetadata?.videoURL, content.name, content.duration)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/stop/{contentId}")
    fun stop(@PathVariable("contentId") uuid: String, @RequestBody botStopContentReader: BotStopContentReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.isPlayable()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasStopMediaPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.stopContent(content.server.guildId!!, botStopContentReader.uid, content.uuid, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/clear/{serverId}")
    fun clearQueue(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasClearQueuePermission(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.clearQueue(server.guildId!!, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/position/{contentId}")
    fun setPosition(@PathVariable("contentId") uuid: String, @RequestBody positionReader: BotPositionReader): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.isPlayable()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasUpdatePositionMediaPermission(content.server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.setContentPosition(content.server.guildId!!, content.uuid, authenticatedUser.userId!!, positionReader.position)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/pause/{serverId}")
    fun pause(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasPauseMediaPermission(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.pauseContent(server.guildId!!, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/resume/{serverId}")
    fun resume(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userService.getAuthenticatedUser() ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.hasResumeMediaPermission(server)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.resumeContent(server.guildId!!, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = botService.toBotServerViewModel(server, reader) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity(viewModel, HttpStatus.OK)
    }
}