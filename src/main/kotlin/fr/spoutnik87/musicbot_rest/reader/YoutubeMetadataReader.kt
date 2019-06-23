package fr.spoutnik87.musicbot_rest.reader

data class YoutubeMetadataReader(
        val id: String,
        val publishedAt: String,
        val channel: String,
        val title: String,
        val description: String
)