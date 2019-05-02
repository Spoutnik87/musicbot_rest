package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.MediaType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MediaTypeRepository : JpaRepository<MediaType, Long> {
    fun findByUuid(uuid: String): MediaType?
}