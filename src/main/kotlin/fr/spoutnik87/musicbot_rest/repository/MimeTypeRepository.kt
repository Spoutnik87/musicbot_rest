package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.MimeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface MimeTypeRepository : JpaRepository<MimeType, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): MimeType?
    @Transactional(readOnly = true)
    fun findByValue(value: String): MimeType?
}