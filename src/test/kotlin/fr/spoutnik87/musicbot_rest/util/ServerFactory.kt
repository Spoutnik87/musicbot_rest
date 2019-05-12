package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.User

class ServerFactory {

    private var server: Server = createDefault().build()

    fun create(uuid: String, name: String, user: User = UserFactory().createBasicUser().build()): ServerFactory {
        server = Server(uuid, name)
        owner(user)
        return this
    }

    fun create(uuid: String, name: String, guildId: String, user: User = UserFactory().createBasicUser().build()): ServerFactory {
        server = Server(uuid, name)
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

    fun build() = server
}