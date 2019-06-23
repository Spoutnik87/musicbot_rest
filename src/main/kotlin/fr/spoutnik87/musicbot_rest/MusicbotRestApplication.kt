package fr.spoutnik87.musicbot_rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
@EnableScheduling
class MusicbotRestApplication {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean(name = ["UUID"])
    fun uuid() = UUID()

    @Bean(name = ["AppConfig"])
    fun appConfig() = AppConfig()

    @Bean
    fun springApplicationContext() = SpringApplicationContext()
}

fun main(args: Array<String>) {
    runApplication<MusicbotRestApplication>(*args)
}