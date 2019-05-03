package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
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
    var extension: String? = null

    @JsonView(Views.Companion.Public::class)
    var size: Int? = null

    @JsonView(Views.Companion.Public::class)
    @Column(nullable = false)
    var content: Boolean = false

    @JsonView(Views.Companion.Public::class)
    @Column(nullable = false)
    var thumbnail: Boolean = false

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

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "media", cascade = [CascadeType.ALL])
    @JsonBackReference
    val mediaGroupSet: MutableSet<MediaGroup> = HashSet()

    val groupList
        get() = mediaGroupSet.map { it.group }

    constructor(uuid: String, name: String, mediaType: MediaType, category: Category) : this(uuid, name) {
        this.mediaType = mediaType
        this.category = category
    }

    fun hasContent() = content

    fun hasThumbnail() = thumbnail
}