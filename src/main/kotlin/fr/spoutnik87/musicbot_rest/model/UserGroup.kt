package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonProperty
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "UserGroup")
data class UserGroup(
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "user_id")
    lateinit var user: User

    @ManyToOne
    @JoinColumn(name = "group_id")
    lateinit var group: Group

    constructor(uuid: String, user: User, group: Group) : this(uuid) {
        this.user = user
        this.group = group
    }

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "user_group_permission", joinColumns = [JoinColumn(name = "user_group_id", referencedColumnName = "id")], inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")])
    lateinit var permissionSet: MutableSet<Permission>

    fun hasPermission(permission: Permission) = permissionSet.any { it.id == permission.id }

    fun hasPermission(permissionEnum: PermissionEnum) = permissionSet.any { it.value == permissionEnum.value }
}