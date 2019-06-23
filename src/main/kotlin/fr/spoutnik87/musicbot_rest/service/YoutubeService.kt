package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.reader.YoutubeMetadataReader
import fr.spoutnik87.musicbot_rest.util.URLHelper
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class YoutubeService {

    fun extractId(link: String) = URLHelper.getQueryParameters(link).firstOrNull { it.first == "v" }?.second

    /**
     * Create Youtube video URL.
     * @param id Youtube video id.
     */
    fun createURL(id: String) = "https://www.youtube.com/watch?v=$id"

    /**
     * @param id Youtube video id.
     */
    fun loadMetadata(id: String): YoutubeMetadataReader? {
        return try {
            val document = Jsoup.connect(createURL(id)).header("User-Agent", "Chrome").get()
            val body = document.body()
            YoutubeMetadataReader(
                    id,
                    body.getElementById("watch-uploader-info").text(),
                    body.getElementById("watch7-user-header").getElementsByClass("yt-user-info")[0].child(0).wholeText(),
                    body.getElementById("eow-title").attr("title"),
                    body.getElementById("watch-description-text").children().text()
            )
        } catch (e: Exception) {
            null
        }
    }
}