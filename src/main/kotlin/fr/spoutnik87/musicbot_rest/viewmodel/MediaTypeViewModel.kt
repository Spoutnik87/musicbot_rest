package fr.spoutnik87.musicbot_rest.viewmodel

import fr.spoutnik87.musicbot_rest.model.MediaType
import java.io.Serializable

data class MediaTypeViewModel(
        val id: String,
        val value: String
) : Serializable {

    companion object {
        fun from(mediaType: MediaType) = MediaTypeViewModel(mediaType.uuid, mediaType.value)
    }
}