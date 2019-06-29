package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.model.YoutubeMetadata
import fr.spoutnik87.musicbot_rest.reader.YoutubeMetadataReader
import fr.spoutnik87.musicbot_rest.repository.YoutubeMetadataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class YoutubeMetadataService {

    @Autowired
    private lateinit var youtubeMetadataRepository: YoutubeMetadataRepository

    /**
     * This method only update the youtubemtadata object, and not the associated content.
     */
    @Transactional
    fun update(youtubeMetadata: YoutubeMetadata, metadata: YoutubeMetadataReader) {
        youtubeMetadata.title = metadata.title.take(255)
        youtubeMetadata.channel = metadata.channel.take(255)
        youtubeMetadata.description = metadata.description.take(5000)
        youtubeMetadata.publishedAt = metadata.publishedAt
        youtubeMetadataRepository.save(youtubeMetadata)
    }
}