package fr.spoutnik87.musicbot_rest.reader

data class BotServerReader(
        val guildId: String,
        val queue: BotQueueReader,
        val currentlyPlaying: BotContentReader?
)