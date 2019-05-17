package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.BotContentReader
import java.io.Serializable

data class BotContentViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        /**
         * User model UUID
         */
        @JsonView(Views.Companion.Public::class)
        val initiator: String,
        @JsonView(Views.Companion.Public::class)
        val duration: Long,
        @JsonView(Views.Companion.Public::class)
        val startTime: Long?
) : Serializable {

    companion object {
        fun from(reader: BotContentReader?, initiator: User): BotContentViewModel? {
            return if (reader != null) {
                BotContentViewModel(reader.id, initiator.uuid, reader.duration, reader.startTime)
            } else {
                null
            }
        }
    }
}