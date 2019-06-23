package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Server

class GroupFactory {

    private var group: Group = createDefault().build()

    fun create(uuid: String, name: String): GroupFactory {
        group = Group(uuid, name, 0)
        return this
    }

    fun server(server: Server, permissions: List<Permission>? = null): GroupFactory {
        group.server = server
        if (permissions != null) {
            permissions(permissions)
        }
        return this
    }

    fun permissions(permissions: List<Permission>): GroupFactory {
        group.permissionSet = permissions.toMutableSet()
        return this
    }

    fun addPermission(permission: Permission): GroupFactory {
        group.permissionSet.add(permission)
        return this
    }

    fun clearPermissions(): GroupFactory {
        group.permissionSet.clear()
        return this
    }

    fun createDefault() = create("groupToken", "Group")

    fun build() = group
}