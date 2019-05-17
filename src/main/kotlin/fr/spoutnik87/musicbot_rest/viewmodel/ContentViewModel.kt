package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.model.Views
import java.io.Serializable

data class ContentViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val extension: String?,
        @JsonView(Views.Companion.Public::class)
        val size: Long?,
        @JsonView(Views.Companion.Public::class)
        val media: Boolean,
        @JsonView(Views.Companion.Public::class)
        val thumbnail: Boolean,
        @JsonView(Views.Companion.Public::class)
        val contentType: ContentTypeViewModel,
        @JsonView(Views.Companion.Public::class)
        val category: CategoryViewModel,
        @JsonView(Views.Companion.Public::class)
        val serverId: String

) : Serializable {

    companion object {
        fun from(content: Content) = ContentViewModel(
                content.uuid,
                content.name,
                content.extension,
                content.size,
                content.media,
                content.thumbnail,
                ContentTypeViewModel.from(content.contentType),
                CategoryViewModel.from(content.category),
                content.groupList[0].server.uuid)
    }
}