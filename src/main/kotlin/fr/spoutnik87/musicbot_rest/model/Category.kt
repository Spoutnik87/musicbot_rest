package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Category")
data class Category(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String,
        @Column(nullable = false)
        var thumbnailSize: Long
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "author_id")
    lateinit var author: User

    @ManyToOne
    @JoinColumn(name = "server_id")
    lateinit var server: Server

    constructor(uuid: String, name: String, thumbnailSize: Long, user: User, server: Server) : this(uuid, name, thumbnailSize) {
        this.author = user
        this.server = server
    }

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL])
    val contentSet: MutableSet<Content> = HashSet()

    fun hasThumbnail() = thumbnailSize > 0
}