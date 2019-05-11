package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.repository.PermissionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class PermissionLoader : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var permissionRepository: PermissionRepository

    @Autowired
    private lateinit var uuid: UUID

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        PermissionEnum.values().forEach {
            permissionRepository.save(Permission(uuid.v4(), it.value))
        }
    }
}