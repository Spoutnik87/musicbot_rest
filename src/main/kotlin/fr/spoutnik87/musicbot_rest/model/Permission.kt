package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "Permission")
data class Permission(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false, unique = true)
        var value: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @ManyToMany(mappedBy = "permissionSet")
    @JsonManagedReference
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    val userList
        get() = userGroupSet.map { it.user }

    val groupList
        get() = userGroupSet.map { it.group }
}