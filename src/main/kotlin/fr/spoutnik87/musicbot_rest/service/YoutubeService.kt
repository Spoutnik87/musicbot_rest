package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.reader.YoutubeMetadataReader
import fr.spoutnik87.musicbot_rest.util.URLHelper
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat

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
            val contents = body.getElementById("watch7-content")
            contents.getElementsByTag("meta")
            var duration: Long = 0
            try {
                var durationText = contents.getElementsByAttributeValue("itemprop", "duration").attr("content").substring(2)
                val minutes = durationText.substringBefore("M").toInt()
                durationText = durationText.substringAfter("M")
                val seconds = durationText.substringBefore("S").toInt()
                duration = (minutes*60 + seconds)*1000L
            } catch (e: Exception) {}
            var publishedAt: Long = 0
            try {
                val format = SimpleDateFormat("YYYY-MM-dd")
                publishedAt = format.parse(contents.getElementsByAttributeValue("itemprop", "datePublished").attr("content")).time
            } catch (e: Exception) {}
            YoutubeMetadataReader(
                    id,
                    publishedAt,
                    body.getElementById("watch7-user-header").getElementsByClass("yt-user-info")[0].child(0).wholeText(),
                    body.getElementById("eow-title").attr("title"),
                    body.getElementById("watch-description-text").children().text(),
                    duration
            )
        } catch (e: Exception) {
            null
        }
    }
}