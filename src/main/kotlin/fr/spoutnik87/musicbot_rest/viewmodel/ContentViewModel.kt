package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.model.Views

data class ContentViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val description: String,
        @JsonView(Views.Companion.Public::class)
        val thumbnailSize: Long?,
        @JsonView(Views.Companion.Public::class)
        val duration: Long?,
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

        @JsonView(Views.Companion.Public::class)
        var localMetadata: LocalMetadataViewModel? = null
        @JsonView(Views.Companion.Public::class)
        var youtubeMetadata: YoutubeMetadataViewModel? = null

    companion object {
        fun from(content: Content): ContentViewModel {
                val viewModel = ContentViewModel(
                        content.uuid,
                        content.name,
                        content.description,
                        content.thumbnailSize,
                        content.duration,
                        content.hasThumbnail(),
                        ContentTypeViewModel.from(content.contentType),
                        CategoryViewModel.from(content.category),
                        content.groupList[0].server.uuid,
                        content.groupList.map { GroupViewModel(it.uuid, it.name, it.server.uuid) })
                if (content.isLocalContent) {
                        val metadata = content.localMetadata
                        if (metadata != null) {
                                viewModel.localMetadata = LocalMetadataViewModel.from(metadata)
                        }
                } else if (content.isYoutubeContent) {
                        val metadata = content.youtubeMetadata
                        if (metadata != null) {
                                viewModel.youtubeMetadata = YoutubeMetadataViewModel.from(metadata)
                        }
                }
                return viewModel
        }
    }
}