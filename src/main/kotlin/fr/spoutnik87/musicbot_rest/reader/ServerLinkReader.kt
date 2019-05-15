package fr.spoutnik87.musicbot_rest.reader

data class ServerLinkReader(
        var userId: String,
        val guildId: String,
        val token: String
) {
}