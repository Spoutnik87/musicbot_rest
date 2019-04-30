package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "GroupTable")
data class Group(
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
    @JoinColumn(name = "server_id")
    @JsonManagedReference
    lateinit var server: Server

    constructor(uuid: String, name: String, server: Server) : this(uuid, name) {
        this.server = server
    }

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    @JsonBackReference
    val userGroupSet: Set<UserGroup> = HashSet()

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    @JsonBackReference
    val mediaGroupSet: Set<MediaGroup> = HashSet()

    val userList
        get() = userGroupSet.map { it.user }

    fun hasUser(user: User) = userGroupSet.any { it.user.id == user.id }
}