package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContentTypeRepository : JpaRepository<ContentType, Long> {
    fun findByUuid(uuid: String): ContentType?
    fun findByValue(value: String): ContentType?
}