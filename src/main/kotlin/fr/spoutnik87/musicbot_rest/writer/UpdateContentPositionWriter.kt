package fr.spoutnik87.musicbot_rest.writer

data class UpdateContentPositionWriter(
        val id: String,
        val initiator: String,
        val position: Long
)