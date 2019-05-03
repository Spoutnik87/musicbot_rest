package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "UserGroup")
data class UserGroup(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    lateinit var user: User

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    lateinit var group: Group

    constructor(uuid: String, user: User, group: Group) : this(uuid) {
        this.user = user
        this.group = group
    }

    @JsonView(Views.Companion.Public::class)
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "user_group_permission", joinColumns = [JoinColumn(name = "user_group_id", referencedColumnName = "id")], inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")])
    @JsonBackReference
    lateinit var permissionSet: MutableSet<Permission>

    fun hasPermission(permission: Permission) = permissionSet.any { it.id == permission.id }

    fun hasPermission(permissionEnum: PermissionEnum) = permissionSet.any { it.value == permissionEnum.value }
}