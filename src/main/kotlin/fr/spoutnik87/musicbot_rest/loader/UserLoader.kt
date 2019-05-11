package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.RoleRepository
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
@Order(4)
class UserLoader : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var appConfig: AppConfig

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        val role = roleRepository.findByName(RoleEnum.BOT.value)
        userRepository.save(User(uuid.v4(), appConfig.botUsername, "Bot", "Bot", "Bot", bCryptPasswordEncoder.encode(appConfig.botPassword), role!!))
    }
}