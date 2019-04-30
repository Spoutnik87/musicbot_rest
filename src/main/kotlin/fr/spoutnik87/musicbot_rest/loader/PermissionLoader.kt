package fr.spoutnik87.musicbot_rest.loader

import fr.spoutnik87.musicbot_rest.UUID
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
        permissionRepository.save(Permission(uuid.v4(), "Créer un media", "CREATE_MEDIA"))
        permissionRepository.save(Permission(uuid.v4(), "Supprimer un media", "DELETE_MEDIA"))
        permissionRepository.save(Permission(uuid.v4(), "Voir un media", "READ_MEDIA"))
        permissionRepository.save(Permission(uuid.v4(), "Changer le mode", "CHANGE_MODE"))
        permissionRepository.save(Permission(uuid.v4(), "Jouer un media", "PLAY_MEDIA"))
        permissionRepository.save(Permission(uuid.v4(), "Arreter un media", "STOP_MEDIA"))
        permissionRepository.save(Permission(uuid.v4(), "Créer une catégorie", "CREATE_CATEGORY"))
        permissionRepository.save(
                Permission(uuid.v4(), "Supprimer une catégorie", "DELETE_CATEGORY"))
    }
}