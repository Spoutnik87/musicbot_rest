package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Category
import fr.spoutnik87.musicbot_rest.model.Media
import fr.spoutnik87.musicbot_rest.model.MediaType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MediaRepository : JpaRepository<Media, Long> {
    fun findByUuid(uuid: String): Optional<Media>
    fun findByMediaType(mediaType: MediaType): List<Media>
    fun findByCategory(category: Category): List<Media>
}