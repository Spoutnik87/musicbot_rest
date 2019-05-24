package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Views
import java.io.Serializable

data class BotServerViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val queue: List<BotContentViewModel>,
        @JsonView(Views.Companion.Public::class)
        val playing: BotContentViewModel?
) : Serializable {

    /*companion object {
        fun from(reader: BotServerReader, server: Server, initiator: User) = BotServerViewModel(server.uuid, reader.queue.trackList.map { BotContentViewModel.from(it, initiator)!! }, BotContentViewModel.from(reader.currentlyPlaying, initiator))
    }*/
}