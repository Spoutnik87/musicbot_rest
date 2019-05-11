package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.ContentType
import fr.spoutnik87.musicbot_rest.model.Views
import java.io.Serializable

data class ContentTypeViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val value: String
) : Serializable {

    companion object {
        fun from(contentType: ContentType) = ContentTypeViewModel(contentType.uuid, contentType.value)
    }
}