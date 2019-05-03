package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Media
import fr.spoutnik87.musicbot_rest.model.Views
import java.io.Serializable

data class MediaViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val extension: String?,
        @JsonView(Views.Companion.Public::class)
        val size: Long?,
        @JsonView(Views.Companion.Public::class)
        val content: Boolean,
        @JsonView(Views.Companion.Public::class)
        val thumbnail: Boolean,
        @JsonView(Views.Companion.Public::class)
        val mediaType: MediaTypeViewModel,
        @JsonView(Views.Companion.Public::class)
        val category: CategoryViewModel

) : Serializable {

    companion object {
        fun from(media: Media) = MediaViewModel(
                media.uuid,
                media.name,
                media.extension,
                media.size,
                media.content,
                media.thumbnail,
                MediaTypeViewModel.from(media.mediaType),
                CategoryViewModel.from(media.category))
    }
}