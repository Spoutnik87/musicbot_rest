package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ContentTypeRepository : JpaRepository<ContentType, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): ContentType?
    @Transactional(readOnly = true)
    fun findByValue(value: String): ContentType?
}