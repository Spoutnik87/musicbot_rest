package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Media")
data class Media(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String
) : AuditModel(), Serializable {

    var extension: String? = null

    var size: Long? = null

    @Column(nullable = false)
    var content: Boolean = false

    @Column(nullable = false)
    var thumbnail: Boolean = false

    @ManyToOne
    @JoinColumn(name = "media_type_id")
    lateinit var mediaType: MediaType

    @ManyToOne
    @JoinColumn(name = "category_id")
    lateinit var category: Category

    @OneToMany(mappedBy = "media", cascade = [CascadeType.ALL])
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