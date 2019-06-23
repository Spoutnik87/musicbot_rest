package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.LocalMetadata
import fr.spoutnik87.musicbot_rest.model.Views

data class LocalMetadataViewModel(
        @JsonView(Views.Companion.Public::class)
        val mimeType: String,
        @JsonView(Views.Companion.Public::class)
        val mediaSize: Long
) : ViewModel {
    companion object {
        fun from(localMetadata: LocalMetadata) = LocalMetadataViewModel(localMetadata.mimeType.value, localMetadata.mediaSize)
    }
}