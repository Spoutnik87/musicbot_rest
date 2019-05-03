package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Server")
data class Server(
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
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    lateinit var owner: User

    constructor(uuid: String, name: String, owner: User) : this(uuid, name) {
        this.owner = owner
    }

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "server", cascade = [CascadeType.ALL])
    @JsonBackReference
    val groupSet: MutableSet<Group> = HashSet()

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "server", cascade = [CascadeType.ALL])
    @JsonBackReference
    val categorySet: MutableSet<Category> = HashSet()

    val userList
        get() = groupSet.flatMap { it.userList }.distinctBy { it.id }

    fun hasUser(user: User) = groupSet.any { it.hasUser(user) }
}