package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.User

class ServerFactory {

    private var server: Server = createDefault().build()

    fun create(uuid: String, name: String, user: User = UserFactory().createBasicUser().build()): ServerFactory {
        server = Server(uuid, name, 0)
        owner(user)
        return this
    }

    fun create(uuid: String, name: String, guildId: String, user: User = UserFactory().createBasicUser().build()): ServerFactory {
        server = Server(uuid, name, 0)
        owner(user)
        server.guildId = guildId
        return this
    }

    fun createDefault() = create("serverToken", "Server")

    fun owner(user: User): ServerFactory {
        server.owner = user
        user.ownedServerSet.add(server)
        return this
    }

    fun link(guildId: String): ServerFactory {
        server.guildId = guildId
        return this
    }

    fun defaultGroup(group: Group): ServerFactory {
        server.defaultGroup = group
        group.server = server
        return this
    }

    fun defaultGroup(uuid: String, name: String, permissions: List<Permission> = ArrayList()): ServerFactory {
        server.defaultGroup = Group(uuid, name, 0, UserFactory().createBasicUser().build(), server, permissions)
        return this
    }

    fun build() = server
}