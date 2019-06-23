package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.MimeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MimeTypeRepository : JpaRepository<MimeType, Long> {
    fun findByUuid(uuid: String): MimeType?
    fun findByValue(value: String): MimeType?
}