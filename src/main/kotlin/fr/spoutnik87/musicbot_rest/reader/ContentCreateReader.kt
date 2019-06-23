package fr.spoutnik87.musicbot_rest.reader

data class ContentCreateReader(
        val visibleGroupList: List<VisibleGroupReader>,
        val categoryId: String,
        val contentType: String,
        val name: String,
        val description: String,
        /**
         * If a content is a Youtube video, a link can be present.
         */
        val link: String?
)