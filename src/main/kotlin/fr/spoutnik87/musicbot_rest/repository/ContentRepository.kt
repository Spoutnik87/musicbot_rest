package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Category
import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.model.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContentRepository : JpaRepository<Content, Long> {
    fun findByUuid(uuid: String): Content?
    fun findByContentType(contentType: ContentType): List<Content>
    fun findByCategory(category: Category): List<Content>
}