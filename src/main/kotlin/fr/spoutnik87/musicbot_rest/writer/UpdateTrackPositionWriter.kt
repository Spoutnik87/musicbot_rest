package fr.spoutnik87.musicbot_rest.writer

data class UpdateTrackPositionWriter(
        val id: String,
        val initiator: String,
        val position: Long
)