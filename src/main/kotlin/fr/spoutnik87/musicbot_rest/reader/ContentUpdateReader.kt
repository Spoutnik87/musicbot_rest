package fr.spoutnik87.musicbot_rest.reader

data class ContentUpdateReader(
        val visibleGroupList: List<VisibleGroupReader>?,
        val categoryId: String?,
        val contentType: String?,
        val name: String?,
        val description: String?,
        val link: String?
)