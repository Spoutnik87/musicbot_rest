package fr.spoutnik87.musicbot_rest.reader

data class ContentCreateReader(
        var groupId: String,
        var categoryId: String,
        var name: String,
        var description: String
) {
}