package fr.spoutnik87.musicbot_rest.viewmodel

import fr.spoutnik87.musicbot_rest.model.Media
import java.io.Serializable

data class MediaViewModel(
        val id: String,
        val name: String,
        val extension: String?,
        val size: Int?,
        val content: Boolean,
        val thumbnail: Boolean,
        val mediaType: MediaTypeViewModel,
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