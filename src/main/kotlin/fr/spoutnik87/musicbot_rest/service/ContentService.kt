package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.ContentGroupRepository
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ContentService {

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Autowired
    private lateinit var contentGroupRepository: ContentGroupRepository

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var uuid: UUID

    /**
     * Create a content with a default group and thumbnail.
     */
    fun create(name: String, description: String, author: User, contentType: ContentType, category: Category, group: Group): Content? {
        if (!validName(name) || !validDescription(description)) {
            return null
        }
        var content = Content(uuid.v4(), name, description, author, contentType, category)
        val thumbnail = imageService.generateRandomImage(content.uuid)
        content.thumbnail = true
        content.thumbnailSize = thumbnail.size.toLong()
        fileService.saveFile(appConfig.contentThumbnailsPath + content.uuid, thumbnail)
        content = contentRepository.save(content)
        val contentGroup = contentGroupRepository.save(ContentGroup(content, group))
        content.contentGroupSet.add(contentGroup)
        return content
    }

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
}