package fr.spoutnik87.musicbot_rest.reader

data class YoutubeMetadataReader(
        val id: String,
        val publishedAt: Long,
        val channel: String,
        val title: String,
        val description: String,
        val duration: Long
)