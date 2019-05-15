package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Server")
data class Server(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String
) : AuditModel(), Serializable {

    /**
     * Discord server unique id
     */
    var guildId: String? = null

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "default_group_id")
    lateinit var defaultGroup: Group

    @ManyToOne
    @JoinColumn(name = "user_id")
    lateinit var owner: User

    constructor(uuid: String, name: String, owner: User) : this(uuid, name) {
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

    fun hasUser(user: User) = groupSet.any { it.hasUser(user) }
}