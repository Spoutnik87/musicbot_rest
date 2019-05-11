package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.Views

data class RoleViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Private::class)
        val lvl: Int
) {

    companion object {

        fun from(role: Role) = RoleViewModel(role.uuid, role.name, role.lvl)
    }
}