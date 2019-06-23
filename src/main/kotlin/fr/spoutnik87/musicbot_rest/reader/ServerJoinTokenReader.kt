package fr.spoutnik87.musicbot_rest.reader

data class ServerJoinTokenReader(
        val guildId: String,
        val userId: String,
        val serverJoinToken: String
)