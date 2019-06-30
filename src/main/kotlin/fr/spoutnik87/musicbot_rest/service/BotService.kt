package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.reader.BotContentReader
import fr.spoutnik87.musicbot_rest.reader.BotServerReader
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.viewmodel.BotContentViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.BotServerViewModel
import fr.spoutnik87.musicbot_rest.writer.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BotService {

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var uuid: UUID

    fun toBotServerViewModel(server: Server, reader: BotServerReader): BotServerViewModel? {
        return BotServerViewModel(server.uuid, reader.queue.trackList.map {
            toBotContentViewModel(it) ?: return null
        }, toBotContentViewModel(reader.currentlyPlaying))
    }

    fun toBotContentViewModel(reader: BotContentReader?): BotContentViewModel? {
        reader ?: return null
        val content = contentRepository.findByUuid(reader.id) ?: return null
        val user = userRepository.findByUserId(reader.initiator) ?: return null
        return BotContentViewModel.from(reader, content, user)
    }

    /**
     * @param guildId Discord server id
     */
    fun getServerStatus(guildId: String): BotServerReader? {
        return try {
            RestTemplate().getForObject(appConfig.botAddress + "/server/$guildId", BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param guildId Discord server id
     * @param contentId Content model UUID
     * @param userId Discord user id
     */
    fun playContent(guildId: String, contentId: String, userId: String, link: String? = null): BotServerReader? {
        return try {
            val body = HttpEntity(PlayContentWriter(uuid.v4(), contentId, userId, link))
            RestTemplate().postForObject(appConfig.botAddress + "/server/play/$guildId", body, BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param guildId Discord server id
     * @param uid Unique id of the content in the queue.
     * @param contentId Content model UUID
     * @param userId Discord user id
     */
    fun stopContent(guildId: String, uid: String, contentId: String, userId: String): BotServerReader? {
        return try {
            val body = HttpEntity(StopContentWriter(uid, contentId, userId))
            RestTemplate().postForObject(appConfig.botAddress + "/server/stop/$guildId", body, BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param guildId Discord server id
     * @param userId Discord user id
     */
    fun clearQueue(guildId: String, userId: String): BotServerReader? {
        return try {
            val body = HttpEntity(ClearContentWriter(userId))
            RestTemplate().postForObject(appConfig.botAddress + "/server/clear/$guildId", body, BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param guildId Discord server id
     * @param contentId Content model UUID
     * @param userId Discord user id
     * @param position New track position in millis
     */
    fun setContentPosition(guildId: String, contentId: String, userId: String, position: Long): BotServerReader? {
        return try {
            val body = HttpEntity(UpdateContentPositionWriter(contentId, userId, position))
            RestTemplate().postForObject(appConfig.botAddress + "/server/position/$guildId", body, BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param guildId Discord server id
     * @param userId Discord user id
     */
    fun pauseContent(guildId: String, userId: String): BotServerReader? {
        return try {
            val body = HttpEntity(PauseContentWriter(userId))
            RestTemplate().postForObject(appConfig.botAddress + "/server/pause/$guildId", body, BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param guildId Discord server id
     * @param userId Discord user id
     */
    fun resumeContent(guildId: String, userId: String): BotServerReader? {
        return try {
            val body = HttpEntity(ResumeContentWriter(userId))
            RestTemplate().postForObject(appConfig.botAddress + "/server/resume/$guildId", body, BotServerReader::class.java)
        } catch (e: Exception) {
            null
        }
    }
}