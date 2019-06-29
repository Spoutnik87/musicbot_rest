package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Category
import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.model.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ContentRepository : JpaRepository<Content, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Content?
    @Transactional(readOnly = true)
    fun findByContentType(contentType: ContentType): List<Content>
    @Transactional(readOnly = true)
    fun findByCategory(category: Category): List<Content>
}