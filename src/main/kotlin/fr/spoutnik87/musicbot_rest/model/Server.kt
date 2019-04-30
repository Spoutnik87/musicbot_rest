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

    @JsonView(Views.Companion.Public::class)
    @OneToOne(mappedBy = "server")
    @JoinColumn(name = "bot_id")
    @JsonBackReference
    lateinit var bot: Bot

    constructor(uuid: String, name: String, owner: User) : this(uuid, name) {
        this.owner = owner
    }

    constructor(uuid: String, name: String, owner: User, bot: Bot) : this(uuid, name, owner) {
        this.bot = bot
    }

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "server", cascade = [CascadeType.ALL])
    @JsonBackReference
    val groupSet: Set<Group> = HashSet()

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "server", cascade = [CascadeType.ALL])
    @JsonBackReference
    val categorySet: Set<Category> = HashSet()

    val userList
        get() = groupSet.flatMap { it.userList }.distinctBy { it.id }

    fun hasUser(user: User) = groupSet.any { it.hasUser(user) }
}