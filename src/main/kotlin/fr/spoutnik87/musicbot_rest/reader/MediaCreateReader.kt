package fr.spoutnik87.musicbot_rest.reader

data class MediaCreateReader(
        var groupId: String,
        var categoryId: String,
        var name: String
) {
}