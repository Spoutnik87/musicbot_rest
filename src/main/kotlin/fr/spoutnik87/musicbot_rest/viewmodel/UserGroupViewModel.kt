package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.model.Views

data class UserGroupViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val serverId: String,
        @JsonView(Views.Companion.Public::class)
        val member: Boolean,
        @JsonView(Views.Companion.Public::class)
        val permissions: List<String>
) {
    companion object {

        fun from(group: Group, user: User): UserGroupViewModel {
            val permissions = ArrayList<String>()
            var member = false
            if (group.hasUser(user)) {
                member = true
                permissions.addAll(user.getPermissions(group).map { it.value })
            }
            return UserGroupViewModel(
                    group.uuid,
                    group.name,
                    group.server.uuid,
                    member,
                    permissions
            )
        }
    }
}