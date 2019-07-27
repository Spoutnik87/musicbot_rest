package fr.spoutnik87.musicbot_rest.reader

data class BotContentReader(
        val uid: String,
        val id: String?,
        val initiator: String?,
        val position: Long?,
        val paused: Boolean?,
        val link: String?,
        val name: String?,
        val duration: Long?
)