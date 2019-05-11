package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Content")
data class Content(
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
    @JoinColumn(name = "content_type_id")
    lateinit var contentType: ContentType

    @ManyToOne
    @JoinColumn(name = "category_id")
    lateinit var category: Category

    @OneToMany(mappedBy = "content", cascade = [CascadeType.ALL])
    val contentGroupSet: MutableSet<ContentGroup> = HashSet()

    val groupList
        get() = contentGroupSet.map { it.group }

    constructor(uuid: String, name: String, contentType: ContentType, category: Category) : this(uuid, name) {
        this.contentType = contentType
        this.category = category
    }

    fun hasContent() = content

    fun hasThumbnail() = thumbnail
}