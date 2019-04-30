package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.SpringApplicationContext
import org.springframework.context.annotation.Bean

class SpringApplicationContextTestConfig {

    @Bean
    fun springApplicationContext() = SpringApplicationContext()
}