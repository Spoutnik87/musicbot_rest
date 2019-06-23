package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views

data class MessageViewModel(
        @JsonView(Views.Companion.Public::class)
        val message: String
) : ViewModel