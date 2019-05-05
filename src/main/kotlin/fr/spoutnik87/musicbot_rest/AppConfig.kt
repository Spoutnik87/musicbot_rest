package fr.spoutnik87.musicbot_rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppConfig {

    @Value("\${application.path}")
    lateinit var applicationPath: String

    @Value("\${application.bot.username}")
    lateinit var botUsername: String

    @Value("\${application.bot.password}")
    lateinit var botPassword: String
}