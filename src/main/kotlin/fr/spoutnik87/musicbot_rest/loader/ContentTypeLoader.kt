package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import fr.spoutnik87.musicbot_rest.model.ContentType
import fr.spoutnik87.musicbot_rest.repository.ContentTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(3)
class ContentTypeLoader : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var contentTypeRepository: ContentTypeRepository

    @Autowired
    private lateinit var uuid: UUID

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        ContentTypeEnum.values().forEach {
            this.contentTypeRepository.findByValue(it.value) ?: this.contentTypeRepository.save(ContentType(uuid.v4(), it.value))
        }
    }
}