package fr.spoutnik87.musicbot_rest.model

import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
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
        var description: String
) : AuditModel(), Serializable {

    var mimeType: String? = null

    var mediaSize: Long? = null

    var thumbnailSize: Long? = null

    var duration: Long? = null

    @Column(nullable = false)
    var media: Boolean = false

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

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "local_metadata_id")
    var localMetadata: LocalMetadata? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "youtube_metadata_id")
    var youtubeMetadata: YoutubeMetadata? = null

    val groupList
        get() = contentGroupSet.map { it.group }

    val server
        get() = groupList[0].server

    val spaceUsed
        get() = (thumbnailSize ?: 0) + (mediaSize ?: 0)

    val isLocalContent
        get() = contentType.value == ContentTypeEnum.LOCAL.value

    val isYoutubeContent
        get() = contentType.value == ContentTypeEnum.YOUTUBE.value

    constructor(uuid: String, name: String, description: String, author: User, contentType: ContentType, category: Category) : this(uuid, name, description) {
        this.author = author
        this.contentType = contentType
        this.category = category
    }

    fun hasMedia() = media

    fun hasThumbnail() = thumbnailSize != null
}