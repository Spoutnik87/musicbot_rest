package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.repository.PermissionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionService {

    @Autowired
    private lateinit var permissionRepository: PermissionRepository

    fun getAllPermissions(): List<Permission>? {
        return try {
            PermissionEnum.values().map { permissionRepository.findByValue(it.value) }.requireNoNulls()
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}