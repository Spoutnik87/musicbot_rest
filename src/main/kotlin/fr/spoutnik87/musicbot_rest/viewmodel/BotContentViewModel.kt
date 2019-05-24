package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Content
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.model.Views
import fr.spoutnik87.musicbot_rest.reader.BotContentReader
import java.io.Serializable

data class BotContentViewModel(
        @JsonView(Views.Companion.Public::class)
        val uid: String,
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val initiator: BotContentInitiatorViewModel,
        @JsonView(Views.Companion.Public::class)
        val duration: Long,
        @JsonView(Views.Companion.Public::class)
        val startTime: Long?,
        @JsonView(Views.Companion.Public::class)
        val position: Long?,
        @JsonView(Views.Companion.Public::class)
        val paused: Boolean?
) : Serializable {

    companion object {
        fun from(reader: BotContentReader?, content: Content, initiator: User): BotContentViewModel? {
            return if (reader != null) {
                BotContentViewModel(reader.uid, reader.id, content.name, BotContentInitiatorViewModel(initiator.uuid, initiator.nickname), content.duration
                        ?: return null, reader.startTime, reader.position, reader.paused)
            } else {
                null
            }
        }
    }
}

/**
 * User model UUID and nickname
 */
data class BotContentInitiatorViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val nickname: String
)