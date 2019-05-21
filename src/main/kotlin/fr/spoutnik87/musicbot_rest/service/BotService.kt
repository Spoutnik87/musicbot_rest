package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.reader.BotContentReader
import fr.spoutnik87.musicbot_rest.reader.BotServerReader
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.viewmodel.BotContentViewModel
import fr.spoutnik87.musicbot_rest.viewmodel.BotServerViewModel
import fr.spoutnik87.musicbot_rest.writer.ClearTrackWriter
import fr.spoutnik87.musicbot_rest.writer.PlayTrackWriter
import fr.spoutnik87.musicbot_rest.writer.StopTrackWriter
import fr.spoutnik87.musicbot_rest.writer.UpdateTrackPositionWriter
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
        return RestTemplate().getForObject(appConfig.botAddress + "/server/$guildId", BotServerReader::class.java)
    }

    /**
     * @param guildId Discord server id
     * @param contentId Content model UUID
     * @param userId Discord user id
     */
    fun addContentToQueue(guildId: String, contentId: String, userId: String): BotServerReader? {
        val body = HttpEntity(PlayTrackWriter(contentId, userId))
        return RestTemplate().postForObject(appConfig.botAddress + "/server/play/$guildId", body, BotServerReader::class.java)
    }

    /**
     * @param guildId Discord server id
     * @param contentId Content model UUID
     * @param userId Discord user id
     */
    fun removeContentFromQueue(guildId: String, contentId: String, userId: String): BotServerReader? {
        val body = HttpEntity(StopTrackWriter(contentId, userId))
        return RestTemplate().postForObject(appConfig.botAddress + "/server/stop/$guildId", body, BotServerReader::class.java)
    }

    /**
     * @param guildId Discord server id
     * @param userId Discord user id
     */
    fun clearQueue(guildId: String, userId: String): BotServerReader? {
        val body = HttpEntity(ClearTrackWriter(userId))
        return RestTemplate().postForObject(appConfig.botAddress + "/server/clear/$guildId", body, BotServerReader::class.java)
    }

    /**
     * @param guildId Discord server id
     * @param contentId Content model UUID
     * @param userId Discord user id
     * @param position New track position in millis
     */
    fun setContentPosition(guildId: String, contentId: String, userId: String, position: Long): BotServerReader? {
        val body = HttpEntity(UpdateTrackPositionWriter(contentId, userId, position))
        return RestTemplate().postForObject(appConfig.botAddress + "/server/position/$guildId", body, BotServerReader::class.java)
    }
}