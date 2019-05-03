package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Role")
data class Role(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String,
        @Column(nullable = false)
        var lvl: Int
) : AuditModel(), Serializable {

    @OneToMany(mappedBy = "role", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonBackReference
    val userSet: MutableSet<User> = HashSet()
}