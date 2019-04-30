package fr.spoutnik87.musicbot_rest.util

import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class BCryptTestConfig {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()
}