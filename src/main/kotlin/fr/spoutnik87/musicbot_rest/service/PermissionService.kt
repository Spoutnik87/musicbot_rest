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

    fun getDefaultCreateServerPermissions(): List<Permission>? {
        return try {
            listOf(
                    permissionRepository.findByValue(PermissionEnum.CREATE_CONTENT.value),
                    permissionRepository.findByValue(PermissionEnum.DELETE_CONTENT.value),
                    permissionRepository.findByValue(PermissionEnum.READ_CONTENT.value),
                    permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value),
                    permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.PAUSE_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.RESUME_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.UPDATE_POSITION_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.CLEAR_QUEUE.value),
                    permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value),
                    permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)
            ).requireNoNulls()
        } catch (e: Exception) {
            null
        }
    }

    fun getDefaultJoinServerPermissions(): List<Permission>? {
        return try {
            listOf(
                    permissionRepository.findByValue(PermissionEnum.CREATE_CONTENT.value),
                    permissionRepository.findByValue(PermissionEnum.DELETE_CONTENT.value),
                    permissionRepository.findByValue(PermissionEnum.READ_CONTENT.value),
                    permissionRepository.findByValue(PermissionEnum.CHANGE_MODE.value),
                    permissionRepository.findByValue(PermissionEnum.PLAY_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.STOP_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.PAUSE_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.RESUME_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.UPDATE_POSITION_MEDIA.value),
                    permissionRepository.findByValue(PermissionEnum.CLEAR_QUEUE.value),
                    permissionRepository.findByValue(PermissionEnum.CREATE_CATEGORY.value),
                    permissionRepository.findByValue(PermissionEnum.DELETE_CATEGORY.value)
            ).requireNoNulls()
        } catch (e: Exception) {
            null
        }
    }
}