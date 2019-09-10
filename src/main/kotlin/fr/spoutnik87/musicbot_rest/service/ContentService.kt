package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.ContentGroupRepository
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedInputStream
import java.util.*

@Service
class ContentService {

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Autowired
    private lateinit var contentTypeService: ContentTypeService

    @Autowired
    private lateinit var contentGroupRepository: ContentGroupRepository

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var youtubeService: YoutubeService

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var mimeTypeService: MimeTypeService

    private val logger = LoggerFactory.getLogger(ContentService::class.java)

    val allContents: List<Content>
        get() = contentRepository.findAll()

    val localContents: List<Content>
        get() = contentRepository.findByContentType(contentTypeService.LOCAL)

    val youtubeContents: List<Content>
        get() = contentRepository.findByContentType(contentTypeService.YOUTUBE)

    /**
     * Create a content with a default thumbnail.
     * Return null if error
     */
    @Transactional
    fun create(name: String, description: String, author: User, contentType: ContentType, category: Category): Content? {
        if (!validName(name) || !validDescription(description)) {
            return null
        }
        val uuid = uuid.v4()
        val thumbnail = imageService.generateRandomImage(uuid)
        var content = Content(uuid, name, description, thumbnail.size.toLong(), author, contentType, category)
        fileService.saveFile(appConfig.contentThumbnailsPath + content.uuid, thumbnail)
        return contentRepository.save(content)
    }

    /**
     * The content must be a youtube content.
     * Fetch and load Youtube Metadata
     * Return null if error
     */
    @Transactional
    fun setYoutubeMetadata(content: Content, link: String): Content? {
        if (!content.isYoutubeContent) {
            return null
        }
        val videoId = youtubeService.extractId(link) ?: return null
        if (!validYoutubeVideoId(videoId)) {
            return null
        }
        var youtubeMetadata = content.youtubeMetadata
        val metadata = youtubeService.loadMetadata(videoId)
        if (metadata != null) {
            content.duration = metadata.duration
            if (youtubeMetadata != null) {
                youtubeMetadata.videoId = videoId
                youtubeMetadata.refreshedAt = Date().time
                youtubeMetadata.publishedAt = metadata.publishedAt
                youtubeMetadata.channel = metadata.channel
                youtubeMetadata.title = metadata.title
                youtubeMetadata.description = metadata.description
                youtubeMetadata.playable = true
            } else {
                youtubeMetadata = YoutubeMetadata(true, Date().time, metadata.publishedAt, videoId, metadata.channel, metadata.title, metadata.description)
                content.youtubeMetadata = youtubeMetadata
            }
        } else {
            if (youtubeMetadata != null) {
                youtubeMetadata.playable = false
            } else {
                content.youtubeMetadata = YoutubeMetadata(false, Date().time, 0, videoId, "", "", "")
            }
        }
        return contentRepository.save(content)
    }

    /**
     * The content must be a youtube content.
     * Fetch and load Youtube Metadata
     * Return null if error
     */
    @Transactional
    fun refreshYoutubeMetadata(content: Content): Content? {
        if (!content.isYoutubeContent) {
            return null
        }
        val youtubeMetadata = content.youtubeMetadata
        if (youtubeMetadata?.videoId == null) {
            return null
        }
        logger.info("Synchronizing YouTube metadata for content with id : ${content.id}")
        val metadata = youtubeService.loadMetadata(youtubeMetadata.videoId)
        if (metadata != null) {
            content.duration = metadata.duration
            youtubeMetadata.refreshedAt = Date().time
            youtubeMetadata.publishedAt = metadata.publishedAt
            youtubeMetadata.channel = metadata.channel.take(255)
            youtubeMetadata.title = metadata.title.take(255)
            youtubeMetadata.description = metadata.description.take(5000)
            youtubeMetadata.playable = true
        } else {
            youtubeMetadata.playable = false
        }
        return contentRepository.save(content)
    }

    @Transactional
    fun setVisible(content: Content, group: Group, visible: Boolean): Content? {
        if (content.groupList.any { it.id == group.id }) {
            content.contentGroupSet.filter { it.group.id == group.id }.forEach {
                it.visible = visible
                contentGroupRepository.save(it)
            }
        } else {
            content.contentGroupSet.add(ContentGroup(content, group, visible))
        }
        return contentRepository.save(content)
    }

    @Transactional
    fun update(content: Content, name: String?, description: String?, category: Category?): Content? {
        var updated = false
        if (validName(name)) {
            content.name = name!!
            updated = true
        }
        if (validDescription(description)) {
            content.description = description!!
            updated = true
        }
        if (category != null) {
            content.category = category
        }
        return if (updated) {
            contentRepository.save(content)
        } else {
            null
        }
    }

    @Transactional
    fun updateThumbnail(content: Content, inputStream: BufferedInputStream): Content? {
        if (!fileService.isImage(inputStream)) {
            return null
        }
        if (content.hasThumbnail()) {
            fileService.deleteFile(appConfig.contentThumbnailsPath + content.uuid)
            content.thumbnailSize = 0
            contentRepository.save(content)
        }
        val resizedThumbnail = try {
            val resized = imageService.resize(inputStream.readBytes(), 400, 400)
            inputStream.close()
            resized
        } catch (e: Exception) {
            inputStream.close()
            return null
        }
        fileService.saveFile(appConfig.contentThumbnailsPath + content.uuid, resizedThumbnail)
        content.thumbnailSize = resizedThumbnail.size.toLong()
        return contentRepository.save(content)
    }

    @Transactional
    fun updateMedia(content: Content, inputStream: BufferedInputStream, fileSize: Long): Content? {
        if (!content.isLocalContent) {
            return null
        }
        inputStream.mark(fileSize.toInt() + 1)
        if (!fileService.isAudio(inputStream)) {
            inputStream.close()
            return null
        }
        inputStream.reset()
        inputStream.mark(fileSize.toInt() + 1)
        val duration = fileService.getAudioFileDuration(inputStream)
        if (duration == null) {
            inputStream.close()
            return null
        }
        inputStream.reset()
        if (content.hasMedia()) {
            fileService.deleteFile(appConfig.contentMediaPath + content.uuid)
            content.localMetadata = null
            content.duration = null
        }
        fileService.saveFile(appConfig.contentMediaPath + content.uuid, inputStream.readBytes())
        content.localMetadata = LocalMetadata(fileSize, mimeTypeService.AUDIO_MPEG)
        content.duration = duration
        inputStream.close()
        return contentRepository.save(content)
    }

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validName(name: String?) = name != null && name.length <= 255

    /**
     * Return true if description is valid.
     * Max length is 2000 characters.
     */
    private fun validDescription(description: String?) = description != null && description.length <= 2000

    /**
     * Return true if video id is valid.
     * This video id will be inserted in the YoutubeMetadata table.
     */
    private fun validYoutubeVideoId(videoId: String?) = videoId != null && videoId.length <= 255
}