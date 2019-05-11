package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.model.Views


data class UserViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Mixed::class)
        val email: String,
        @JsonView(Views.Companion.Public::class)
        val nickname: String,
        @JsonView(Views.Companion.Mixed::class)
        val firstname: String,
        @JsonView(Views.Companion.Mixed::class)
        val lastname: String,
        @JsonView(Views.Companion.Mixed::class)
        val role: RoleViewModel
) {

    companion object {

        fun from(user: User) = UserViewModel(user.uuid, user.email, user.nickname, user.firstname, user.lastname, RoleViewModel.from(user.role))
    }
}