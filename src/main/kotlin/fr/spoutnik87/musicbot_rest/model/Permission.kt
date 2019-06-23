package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "Permission")
data class Permission(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false, unique = true)
        var value: String
) : AuditModel(), Serializable {

    @ManyToMany(mappedBy = "permissionSet")
    val groupSet: MutableSet<Group> = HashSet()

    val serverList
        get() = groupSet.map { it.server }.distinctBy { it.id }
}