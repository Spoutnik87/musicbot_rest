package fr.spoutnik87.musicbot_rest.reader

data class BotContentReader(
        val id: String,
        val initiator: String,
        val duration: Long,
        val startTime: Long?
)