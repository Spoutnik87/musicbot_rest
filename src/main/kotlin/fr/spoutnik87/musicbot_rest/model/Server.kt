package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Server")
data class Server(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String,
        @Column(nullable = false)
        var thumbnailSize: Long
) : AuditModel(), Serializable {

    /**
     * Discord server unique id
     */
    var guildId: String? = null

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "default_group_id")
    lateinit var defaultGroup: Group

    @ManyToOne
    @JoinColumn(name = "author_id")
    lateinit var author: User

    @ManyToOne
    @JoinColumn(name = "owner_id")
    lateinit var owner: User

    constructor(uuid: String, name: String, thumbnailSize: Long, author: User, owner: User) : this(uuid, name, thumbnailSize) {
        this.author = author
        this.owner = owner
    }

    @OneToMany(mappedBy = "server", cascade = [CascadeType.ALL])
    val groupSet: MutableSet<Group> = HashSet()

    @OneToMany(mappedBy = "server", cascade = [CascadeType.ALL])
    val categorySet: MutableSet<Category> = HashSet()

    val userList
        get() = groupSet.flatMap { it.userList }.distinctBy { it.id }

    val isLinked
        get() = guildId != null

    val contentList
        get() = groupSet.flatMap { it.contentList }.distinctBy { it.id }

    val visibleContentList
        get() = groupSet.flatMap { it.visibleContentList }.distinctBy { it.id }

    fun hasUser(user: User) = groupSet.any { it.hasUser(user) }

    fun hasGroup(group: Group) = groupSet.any { it.id == group.id }

    fun hasContent(content: Content) = contentList.any { it.id == content.id }
}