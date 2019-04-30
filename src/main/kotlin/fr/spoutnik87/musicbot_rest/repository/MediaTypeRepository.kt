package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.MediaType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MediaTypeRepository : JpaRepository<MediaType, Long> {
    fun findByUuid(uuid: String): Optional<MediaType>
}