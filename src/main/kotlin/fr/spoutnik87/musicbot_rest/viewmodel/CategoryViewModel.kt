package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Category
import fr.spoutnik87.musicbot_rest.model.Views
import java.io.Serializable

data class CategoryViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val serverId: String
) : Serializable {

    companion object {
        fun from(category: Category) = CategoryViewModel(category.uuid, category.name, category.server.uuid)
    }
}