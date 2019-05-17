package fr.spoutnik87.musicbot_rest.controller

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.BotPositionReader
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.service.BotService
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import fr.spoutnik87.musicbot_rest.viewmodel.BotContentViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.BotServerViewModel
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

    @JsonView(Views.Companion.Public::class)
    @GetMapping("/{serverId}")
    fun getStatus(@PathVariable("serverId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!server.hasUser(authenticatedUser)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.getServerStatus(server.guildId!!) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val viewModel = BotServerViewModel(server.uuid, reader.queue.trackList.map {
            val user = userRepository.findByUserId(it.initiator)
            BotContentViewModel.from(it, user!!)!!
        }, BotContentViewModel.from(reader.currentlyPlaying, userRepository.findByUserId(reader.currentlyPlaying?.id!!)!!))
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/play/{contentId}")
    fun play(@PathVariable("contentId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.hasMedia()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasPlayMediaPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.addContentToQueue(content.server.guildId!!, content.uuid, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)

        val viewModel = BotServerViewModel(server.uuid, reader.queue.trackList.map {
            val user = userRepository.findByUserId(it.initiator)
            BotContentViewModel.from(it, user!!)!!
        }, BotContentViewModel.from(reader.currentlyPlaying, userRepository.findByUserId(reader.currentlyPlaying?.id!!)!!))
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/stop/{contentId}")
    fun stop(@PathVariable("contentId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.hasMedia()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasStopMediaPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.removeContentFromQueue(content.server.guildId!!, content.uuid, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)

        val viewModel = BotServerViewModel(server.uuid, reader.queue.trackList.map {
            val user = userRepository.findByUserId(it.initiator)
            BotContentViewModel.from(it, user!!)!!
        }, BotContentViewModel.from(reader.currentlyPlaying, userRepository.findByUserId(reader.currentlyPlaying?.id!!)!!))
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/clear/{contentId}")
    fun clearQueue(@PathVariable("contentId") uuid: String): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.hasMedia()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasStopMediaPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.clearQueue(content.server.guildId!!, authenticatedUser.userId!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)

        val viewModel = BotServerViewModel(server.uuid, reader.queue.trackList.map {
            val user = userRepository.findByUserId(it.initiator)
            BotContentViewModel.from(it, user!!)!!
        }, BotContentViewModel.from(reader.currentlyPlaying, userRepository.findByUserId(reader.currentlyPlaying?.id!!)!!))
        return ResponseEntity(viewModel, HttpStatus.OK)
    }

    @JsonView(Views.Companion.Public::class)
    @PostMapping("/position/{contentId}")
    fun setPosition(@PathVariable("contentId") uuid: String, @RequestBody positionReader: BotPositionReader): ResponseEntity<Any> {
        val authenticatedUser = userRepository.findByEmail(AuthenticationHelper.getAuthenticatedUserEmail()!!)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!authenticatedUser.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val content = contentRepository.findByUuid(uuid) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        if (!content.hasMedia()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (!authenticatedUser.hasPlayMediaPermission(content)) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }
        if (!content.server.isLinked) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val reader = botService.setContentPosition(content.server.guildId!!, content.uuid, authenticatedUser.userId!!, positionReader.position)
                ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val server = serverRepository.findByGuildId(reader.guildId) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)

        val viewModel = BotServerViewModel(server.uuid, reader.queue.trackList.map {
            val user = userRepository.findByUserId(it.initiator)
            BotContentViewModel.from(it, user!!)!!
        }, BotContentViewModel.from(reader.currentlyPlaying, userRepository.findByUserId(reader.currentlyPlaying?.id!!)!!))
        return ResponseEntity(viewModel, HttpStatus.OK)
    }
}