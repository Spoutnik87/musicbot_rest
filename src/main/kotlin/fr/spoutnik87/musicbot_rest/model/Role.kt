package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Role")
data class Role(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var name: String,
        @JsonView(Views.Companion.Public::class)
        var lvl: Int
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "role", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonBackReference
    val userSet: Set<User> = HashSet()
}