package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.MediaGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MediaGroupRepository : JpaRepository<MediaGroup, Long> {
    fun findByUuid(uuid: String): Optional<MediaGroup>
}