package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "Permission")
data class Permission(
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false, unique = true)
        var value: String
) : AuditModel(), Serializable {

    @ManyToMany(mappedBy = "permissionSet")
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    val userList
        get() = userGroupSet.map { it.user }

    val groupList
        get() = userGroupSet.map { it.group }
}