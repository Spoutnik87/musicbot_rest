package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.security.SecurityConfiguration
import org.springframework.context.annotation.Bean

class SecurityConfigurationTestConfig {

    @Bean
    fun securityConfiguration() = SecurityConfiguration()
}