package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Media")
data class Media(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var name: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "media_type_id")
    @JsonManagedReference
    lateinit var mediaType: MediaType

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    lateinit var category: Category

    constructor(uuid: String, name: String, mediaType: MediaType, category: Category) : this(uuid, name) {
        this.mediaType = mediaType
        this.category = category
    }
}