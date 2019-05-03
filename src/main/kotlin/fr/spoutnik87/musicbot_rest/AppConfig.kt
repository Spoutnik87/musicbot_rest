package fr.spoutnik87.musicbot_rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppConfig {

    @Value("\${application.path}")
    lateinit var applicationPath: String
}