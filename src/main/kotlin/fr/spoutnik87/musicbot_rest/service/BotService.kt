package fr.spoutnik87.musicbot_rest.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.reader.BotContentReader
import fr.spoutnik87.musicbot_rest.reader.BotServerReader
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.viewmodel.BotContentViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.BotServerViewModel
import fr.spoutnik87.musicbot_rest.writer.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

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

    @Autowired
    private lateinit var webSocketPlayerService: WebSocketPlayerService

    @Autowired
    private lateinit var serverRepository: ServerRepository

    private var states: LoadingCache<String, BotServerViewModel>? = null

    init {
        states = Caffeine.newBuilder().maximumSize(10000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .refreshAfterWrite(10, TimeUnit.MINUTES)
                .build { getServerStatus(it).let { res -> serverRepository.findByGuildId(it)?.let { server -> res?.let { state -> toBotServerViewModel(server, state) } } } }
    }

    private fun checkUpdateByGuildId(guildId: String, oldState: BotServerReader?, newState: BotServerReader?) {
        if (oldState != newState) {

        }
    }

    /**
     * @param id Server's UUID
     */
    private fun checkUpdate(id: String, oldState: BotServerReader?, newState: BotServerReader) {
        if (oldState != newState) {
            serverRepository.findByGuildId(newState.guildId)?.let { toBotServerViewModel(it, newState) }?.let {
                webSocketPlayerService.sendMessage(id, it)
            }
        }
    }

    fun toBotServerViewModel(server: Server, reader: BotServerReader): BotServerViewModel? {
        return BotServerViewModel(server.uuid, reader.queue.trackList.map {
            toBotContentViewModel(it) ?: return null
        }, toBotContentViewModel(reader.currentlyPlaying))
    }

    fun toBotContentViewModel(reader: BotContentReader?): BotContentViewModel? {
        reader ?: return null
        val content = reader.id?.let { contentRepository.findByUuid(it) }
        val user = reader.initiator?.let { userRepository.findByUserId(it) }
        return BotContentViewModel.from(reader, content, user, reader.link)
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

    fun getCachedServerStatus(guildId: String): BotServerViewModel? {
        return states?.get(guildId)
    }

    fun updateServerStatus(guildId: String, botServerViewModel: BotServerViewModel) {
        states?.put(guildId, botServerViewModel)
    }

    /**
     * @param guildId Discord server id
     * @param contentId Content model UUID
     * @param userId Discord user id
     */
    fun playContent(guildId: String, contentId: String, userId: String, link: String? = null, name: String, duration: Long?): BotServerReader? {
        return try {
            val body = HttpEntity(PlayContentWriter(uuid.v4(), contentId, userId, link, name, duration))
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