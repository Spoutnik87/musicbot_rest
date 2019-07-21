package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(2)
class RoleLoader : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var uuid: UUID

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        RoleEnum.values().forEach {
            roleRepository.findByName(it.value) ?: roleRepository.save(Role(uuid.v4(), it.value, it.lvl))
        }
    }
}