package fr.spoutnik87.musicbot_rest.reader

data class BotContentReader(
        val uid: String,
        val id: String,
        val initiator: String,
        val startTime: Long?,
        val position: Long?,
        val paused: Boolean?
)