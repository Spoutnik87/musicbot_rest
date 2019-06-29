package fr.spoutnik87.musicbot_rest.synchronizers

import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.service.ContentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class YoutubeSynchronizer {

    @Autowired
    private lateinit var contentService: ContentService

    @Scheduled(fixedRate = 900000)
    fun task() {
        val currentTimeMillis = Date().time
        contentService.youtubeContents.filter { it.youtubeMetadata != null }.filter { isRefreshable(it, currentTimeMillis) }.forEach {
            contentService.refreshYoutubeMetadata(it)
        }
    }

    /**
     * A content is refreshed each day.
     */
    fun isRefreshable(content: Content, currentTimeMillis: Long = Date().time): Boolean {
        val youtubeMetadata = content.youtubeMetadata
        return if (youtubeMetadata != null) {
            youtubeMetadata.refreshedAt + 86400000 <= currentTimeMillis
        } else {
            false
        }
    }
}