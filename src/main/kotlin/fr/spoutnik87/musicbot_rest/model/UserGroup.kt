package fr.spoutnik87.musicbot_rest.model

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "UserGroup")
data class UserGroup(
        @ManyToOne
        @JoinColumn(name = "user_id")
        var user: User,
        @ManyToOne
        @JoinColumn(name = "group_id")
        var group: Group
) : AuditModel(), Serializable {

    /*@ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "user_group_permission", joinColumns = [JoinColumn(name = "user_group_id", referencedColumnName = "id")], inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")])
    var permissionSet: MutableSet<Permission> = HashSet()

    constructor(user: User, group: Group, permissions: List<Permission>) : this(user, group) {
        permissionSet.addAll(permissions)
    }

    fun hasPermission(permission: Permission) = permissionSet.any { it.id == permission.id }

    fun hasPermission(permissionEnum: PermissionEnum) = permissionSet.any { it.value == permissionEnum.value }*/
}