package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.MediaTypeEnum
import fr.spoutnik87.musicbot_rest.model.MediaType
import fr.spoutnik87.musicbot_rest.repository.MediaTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(3)
class MediaTypeLoader : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var mediaTypeRepository: MediaTypeRepository

    @Autowired
    private lateinit var uuid: UUID

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        MediaTypeEnum.values().forEach {
            this.mediaTypeRepository.save(MediaType(uuid.v4(), it.value))
        }
    }
}