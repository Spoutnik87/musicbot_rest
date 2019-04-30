package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "UserTable")
data class User(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false, unique = true)
        var email: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false, unique = true)
        var nickname: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var firstname: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var lastname: String,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Column(nullable = false)
        var password: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "role_id")
    @JsonManagedReference
    lateinit var role: Role

    constructor(uuid: String, email: String, nickname: String, firstname: String, lastname: String, password: String, role: Role) : this(uuid, email, nickname, firstname, lastname, password) {
        this.role = role
    }

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    @JsonBackReference
    val userGroupSet: Set<UserGroup> = HashSet()

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL])
    @JsonBackReference
    val serverSet: Set<Server> = HashSet()

    fun isOwner(server: Server) = serverSet.any { it.id == server.id }
}