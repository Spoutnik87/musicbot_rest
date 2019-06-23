package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.model.YoutubeMetadata

data class YoutubeMetadataViewModel(
        @JsonView(Views.Companion.Public::class)
        val videoId: String
) : ViewModel {
    companion object {
        fun from(youtubeMetadata: YoutubeMetadata) = YoutubeMetadataViewModel(youtubeMetadata.videoId)
    }
}