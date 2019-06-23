package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views

data class UserServerJoinTokenViewModel(
        @JsonView(Views.Companion.Public::class)
        val serverJoinToken: String
) : ViewModel {
}