package fr.spoutnik87.musicbot_rest.writer

data class PlayContentWriter(
        val uid: String,
        val id: String,
        val initiator: String,
        val link: String? = null,
        val name: String,
        val duration: Long?
)