package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "MediaGroup")
data class MediaGroup(
        @Column(nullable = false, unique = true)
        var uuid: String
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "media_id")
    lateinit var media: Media

    @ManyToOne
    @JoinColumn(name = "group_id")
    lateinit var group: Group

    constructor(uuid: String, media: Media, group: Group) : this(uuid) {
        this.media = media
        this.group = group
    }
}