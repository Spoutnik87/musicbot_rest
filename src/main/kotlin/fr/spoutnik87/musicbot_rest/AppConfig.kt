package fr.spoutnik87.musicbot_rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppConfig {

    @Value("\${application.path}")
    lateinit var applicationPath: String

    @Value("\${application.bot.host}")
    lateinit var botHost: String

    @Value("\${application.bot.port}")
    lateinit var botPort: String

    @Value("\${application.bot.username}")
    lateinit var botUsername: String

    @Value("\${application.bot.password}")
    lateinit var botPassword: String

    val botAddress
        get() = "http://$botHost:$botPort"

    val contentMediaPath
        get() = "$applicationPath/contents/media/"

    val contentThumbnailsPath
        get() = "$applicationPath/contents/thumbnails/"

    val userThumbnailsPath
        get() = "$applicationPath/users/thumbnails/"

    val serverThumbnailsPath
        get() = "$applicationPath/servers/thumbnails/"

    val groupThumbnailsPath
        get() = "$applicationPath/groups/thumbnails/"

    val categoryThumbnailsPath
        get() = "$applicationPath/categories/thumbnails/"
}