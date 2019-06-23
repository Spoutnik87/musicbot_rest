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
        val description: String,
        @JsonView(Views.Companion.Public::class)
        val mimeType: String?,
        @JsonView(Views.Companion.Public::class)
        val mediaSize: Long?,
        @JsonView(Views.Companion.Public::class)
        val thumbnailSize: Long?,
        @JsonView(Views.Companion.Public::class)
        val duration: Long?,
        @JsonView(Views.Companion.Public::class)
        val media: Boolean,
        @JsonView(Views.Companion.Public::class)
        val thumbnail: Boolean,
        @JsonView(Views.Companion.Public::class)
        val contentType: ContentTypeViewModel,
        @JsonView(Views.Companion.Public::class)
        val category: CategoryViewModel,
        @JsonView(Views.Companion.Public::class)
        val serverId: String,
        @JsonView(Views.Companion.Public::class)
        val groups: List<GroupViewModel>
) : ViewModel {

    companion object {
        fun from(content: Content) = ContentViewModel(
                content.uuid,
                content.name,
                content.description,
                content.mimeType,
                content.mediaSize,
                content.thumbnailSize,
                content.duration,
                content.media,
                content.hasThumbnail(),
                ContentTypeViewModel.from(content.contentType),
                CategoryViewModel.from(content.category),
                content.groupList[0].server.uuid,
                content.groupList.map { GroupViewModel(it.uuid, it.name, it.server.uuid) })
    }
}