package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "MediaGroup")
data class MediaGroup(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "media_id")
    @JsonManagedReference
    lateinit var media: Media

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    lateinit var group: Group

    constructor(uuid: String, media: Media, group: Group) : this(uuid) {
        this.media = media
        this.group = group
    }
}