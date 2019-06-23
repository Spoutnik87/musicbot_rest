package fr.spoutnik87.musicbot_rest.model

import fr.spoutnik87.musicbot_rest.reader.YoutubeMetadataReader
import java.io.Serializable
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "YoutubeMetadata")
data class YoutubeMetadata(
        /**
         * True if Youtube video is accessible.
         */
        @Column(nullable = false)
        var playable: Boolean,
        /**
         * Date of the last synchronisation.
         */
        @Column(nullable = false)
        var refreshedAt: Long,
        @Column(nullable = false)
        var publishedAt: String,
        @Column(nullable = false)
        var videoId: String,
        @Column(nullable = false)
        var channel: String,
        @Column(nullable = false)
        var title: String,
        @Column(nullable = false)
        var description: String
) : AuditModel(), Serializable {

        @OneToOne(mappedBy = "youtubeMetadata")
        val content: Content? = null

        /**
         * Load fields from metadata reader object.
         */
        fun from(metadata: YoutubeMetadataReader) {
                title = metadata.title
                channel = metadata.channel
                description = metadata.description
                publishedAt = metadata.publishedAt
        }
}