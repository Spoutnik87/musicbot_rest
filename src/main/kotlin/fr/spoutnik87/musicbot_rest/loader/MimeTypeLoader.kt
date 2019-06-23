package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.MimeTypeEnum
import fr.spoutnik87.musicbot_rest.model.MimeType
import fr.spoutnik87.musicbot_rest.repository.MimeTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(5)
class MimeTypeLoader : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var mimeTypeRepository: MimeTypeRepository

    @Autowired
    private lateinit var uuid: UUID

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        MimeTypeEnum.values().forEach {
            mimeTypeRepository.save(MimeType(uuid.v4(), it.value))
        }
    }
}