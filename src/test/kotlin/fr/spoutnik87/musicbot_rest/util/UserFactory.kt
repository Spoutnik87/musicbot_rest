package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.SpringApplicationContext
import fr.spoutnik87.musicbot_rest.model.*

class UserFactory {

    private val bCryptPasswordEncoder = SpringApplicationContext.bCryptPasswordEncoder

    private var user: User = createBasicUser().build()

    fun create(uuid: String, email: String, nickname: String, firstname: String, lastname: String,
               password: String, role: Role = Role("userRoleToken", "USER", 2)): UserFactory {
        user = User(uuid, email, nickname, firstname, lastname, password, role, 0)
        return this
    }

    fun createBasicUser(): UserFactory {
        return create("basicUserToken", "user@test.com", "Nickname", "Firstname",
                "Lastname", bCryptPasswordEncoder.encode("password"),
                Role("userRoleToken", "USER", 2))
    }

    fun createAdminUser(): UserFactory {
        return create(
                "adminUserToken",
                "admin@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                Role("adminRoleToken", "ADMIN", 1))
    }

    fun createBotUser(): UserFactory {
        return create(
                "botUserToken",
                "bot@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                Role("botRoleToken", "BOT", 3))
    }

    fun role(role: Role): UserFactory {
        user.role = role
        return this
    }

    fun flushServers(): UserFactory {
        user.userGroupSet.clear()
        return this
    }

    fun inServer(group: Group, server: Server, owner: Boolean = false): UserFactory {
        val userGroup = UserGroup(user, group)
        user.userGroupSet.add(userGroup)
        group.userGroupSet.add(userGroup)
        group.server = server
        server.groupSet.add(group)
        if (owner || server.userList.size == 1) {
            user.ownedServerSet.add(server)
            server.owner = user
        }
        return this
    }

    fun build() = user
}