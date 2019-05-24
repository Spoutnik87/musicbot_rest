package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Content")
data class Content(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String,
        @Column(nullable = false, length = 2000)
        val description: String
) : AuditModel(), Serializable {

    var mimeType: String? = null

    var mediaSize: Long? = null

    var thumbnailSize: Long? = null

    var duration: Long? = null

    @Column(nullable = false)
    var media: Boolean = false

    @Column(nullable = false)
    var thumbnail: Boolean = false

    @ManyToOne
    @JoinColumn(name = "author_id")
    lateinit var author: User

    @ManyToOne
    @JoinColumn(name = "content_type_id")
    lateinit var contentType: ContentType

    @ManyToOne
    @JoinColumn(name = "category_id")
    lateinit var category: Category

    @OneToMany(mappedBy = "content", cascade = [CascadeType.ALL])
    val contentGroupSet: MutableSet<ContentGroup> = HashSet()

    val groupList
        get() = contentGroupSet.map { it.group }

    val server
        get() = groupList[0].server

    val spaceUsed
        get() = (thumbnailSize ?: 0) + (mediaSize ?: 0)

    constructor(uuid: String, name: String, description: String, author: User, contentType: ContentType, category: Category) : this(uuid, name, description) {
        this.author = author
        this.contentType = contentType
        this.category = category
    }

    fun hasMedia() = media

    fun hasThumbnail() = thumbnail
}