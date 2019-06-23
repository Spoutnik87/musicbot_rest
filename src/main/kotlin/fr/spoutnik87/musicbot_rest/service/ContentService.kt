package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.ContentGroupRepository
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    val allContents: List<Content>
        get() = contentRepository.findAll()

    val localContents: List<Content>
        get() = contentRepository.findByContentType(contentTypeService.LOCAL)

    val youtubeContents: List<Content>
        get() = contentRepository.findByContentType(contentTypeService.YOUTUBE)

    /**
     * Create a content with a default group and thumbnail.
     * Return null if error
     */
    @Transactional
    fun create(name: String, description: String, author: User, contentType: ContentType, category: Category, group: Group? = null): Content? {
        if (!validName(name) || !validDescription(description)) {
            return null
        }
        var content = Content(uuid.v4(), name, description, author, contentType, category)
        val thumbnail = imageService.generateRandomImage(content.uuid)
        content.thumbnailSize = thumbnail.size.toLong()
        fileService.saveFile(appConfig.contentThumbnailsPath + content.uuid, thumbnail)
        content = contentRepository.save(content)
        if (group != null) {
            val contentGroup = contentGroupRepository.save(ContentGroup(content, group, true))
            content.contentGroupSet.add(contentGroup)
        }
        return content
    }

    /**
     * The content must be a youtube content.
     * Fetch and load Youtube Metadata
     * Return null if error
     */
    fun setYoutubeMetadata(content: Content, link: String): Content? {
        if (!content.isYoutubeContent) {
            return null
        }
        val videoId = youtubeService.extractId(link)
        if (!validYoutubeVideoId(videoId)) {
            return null
        }
        var youtubeMetadata = content.youtubeMetadata
        val metadata = youtubeService.loadMetadata(videoId!!) ?: return null
        if (youtubeMetadata != null) {
            youtubeMetadata.videoId = videoId
            youtubeMetadata.refreshedAt = java.util.Date().time
            youtubeMetadata.from(metadata)
            youtubeMetadata.playable = true
        } else {
            youtubeMetadata = YoutubeMetadata(true, Date().time, metadata.publishedAt, videoId, metadata.channel, metadata.title, metadata.description)
            content.youtubeMetadata = youtubeMetadata
        }
        return contentRepository.save(content)
    }

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