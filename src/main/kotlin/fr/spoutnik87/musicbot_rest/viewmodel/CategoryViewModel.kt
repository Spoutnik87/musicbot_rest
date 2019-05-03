package fr.spoutnik87.musicbot_rest.viewmodel

import fr.spoutnik87.musicbot_rest.model.Category
import java.io.Serializable

data class CategoryViewModel(
        val id: String,
        val name: String,
        val serverId: String
) : Serializable {

    companion object {
        fun from(category: Category) = CategoryViewModel(category.uuid, category.name, category.server.uuid)
    }
}