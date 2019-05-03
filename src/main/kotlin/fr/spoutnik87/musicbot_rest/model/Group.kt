package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "GroupTable")
data class Group(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "server_id")
    lateinit var server: Server

    constructor(uuid: String, name: String, server: Server) : this(uuid, name) {
        this.server = server
    }

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val mediaGroupSet: MutableSet<MediaGroup> = HashSet()

    val userList
        get() = userGroupSet.map { it.user }

    val mediaList
        get() = mediaGroupSet.map { it.media }

    fun hasUser(user: User) = userGroupSet.any { it.user.id == user.id }

    fun hasMedia(media: Media) = mediaGroupSet.any { it.media.id == media.id }
}