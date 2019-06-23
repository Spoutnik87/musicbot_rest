package fr.spoutnik87.musicbot_rest.constant

enum class ContentTypeEnum(
        val value: String
) {
    /**
     * Default content type.
     */
    EMPTY("EMPTY"),
    /**
     * Content type for local music file uploaded by the user.
     */
    LOCAL("LOCAL"),
    /**
     * Content type for Youtube video.
     */
    YOUTUBE("YOUTUBE")
}