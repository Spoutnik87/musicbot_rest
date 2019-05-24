package fr.spoutnik87.musicbot_rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class JSONConfiguration {

    @Autowired
    fun configureJackson(objectMapper: ObjectMapper) {
        objectMapper.registerModule(KotlinModule())
    }
}