package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.YoutubeMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface YoutubeMetadataRepository : JpaRepository<YoutubeMetadata, Long>