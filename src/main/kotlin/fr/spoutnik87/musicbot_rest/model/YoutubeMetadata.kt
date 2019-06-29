package fr.spoutnik87.musicbot_rest.model

import fr.spoutnik87.musicbot_rest.exception.InvalidModelFIeldException
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "YoutubeMetadata")
class YoutubeMetadata(
        _playable: Boolean,
        _refreshedAt: Long,
        _publishedAt: Long,
        _videoId: String,
        _channel: String,
        _title: String,
        _description: String
) : AuditModel(), Serializable {

        /**
         * True if Youtube video is accessible.
         */
        @Column(nullable = false)
        var playable = _playable
        /**
         * Date of the last synchronisation.
         */
        @Column(nullable = false)
        var refreshedAt = _refreshedAt
        @Column(nullable = false)
        var publishedAt = _publishedAt
                set(value) {
                        if (value >= 0) {
                                field = value
                        } else {
                                throw InvalidModelFIeldException("YoutubeMetadata", "publishedAt", value)
                        }
                }
        @Column(nullable = false)
        var videoId = _videoId
                set(value) {
                        if (value.length <= 255) {
                                field = value
                        } else {
                                throw InvalidModelFIeldException("YoutubeMetadata", "videoId", value)
                        }
                }
        @Column(nullable = false)
        var channel = _channel
                set(value) {
                        if (value.length <= 255) {
                                field = value
                        } else {
                                throw InvalidModelFIeldException("YoutubeMetadata", "channel", value)
                        }
                }
        @Column(nullable = false)
        var title = _title
                set(value) {
                        if (value.length <= 255) {
                                field = value
                        } else {
                                throw InvalidModelFIeldException("YoutubeMetadata", "title", value)
                        }
                }
        @Column(nullable = false, columnDefinition = "TEXT")
        var description = _description
                set(value) {
                        if (value.length <= 100000) {
                                field = value
                        } else {
                                throw InvalidModelFIeldException("YoutubeMetadata", "publishedAt", value)
                        }
                }

        @OneToOne(mappedBy = "youtubeMetadata")
        val content: Content? = null
}