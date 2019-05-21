package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.ContentGroupRepository
import fr.spoutnik87.musicbot_rest.repository.ContentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ContentService {

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Autowired
    private lateinit var contentGroupRepository: ContentGroupRepository

    @Autowired
    private lateinit var uuid: UUID

    fun save(name: String, author: User, contentType: ContentType, category: Category, group: Group): Content {
        var content = Content(uuid.v4(), name, author, contentType, category)
        content = contentRepository.save(content)
        var contentGroup = ContentGroup(content, group)
        contentGroup = contentGroupRepository.save(contentGroup)
        content.contentGroupSet.add(contentGroup)
        return content
    }
}