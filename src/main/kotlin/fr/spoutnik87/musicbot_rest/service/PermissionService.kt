package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.exception.InitialPermissionNotFoundException
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.repository.PermissionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionService {

    @Autowired
    private lateinit var permissionRepository: PermissionRepository

    val allPermissions: List<Permission>
        get() = permissionRepository.findAll()

    val allInitialPermissions
        get() = PermissionEnum.values().map { getByValue(it) }

    val CREATE_CONTENT
        get() = getByValue(PermissionEnum.CREATE_CONTENT)

    val DELETE_CONTENT
        get() = getByValue(PermissionEnum.DELETE_CONTENT)

    val READ_CONTENT
        get() = getByValue(PermissionEnum.READ_CONTENT)

    val CHANGE_MODE
        get() = getByValue(PermissionEnum.CHANGE_MODE)

    val PLAY_MEDIA
        get() = getByValue(PermissionEnum.PLAY_MEDIA)

    val STOP_MEDIA
        get() = getByValue(PermissionEnum.STOP_MEDIA)

    val PAUSE_MEDIA
        get() = getByValue(PermissionEnum.PAUSE_MEDIA)

    val RESUME_MEDIA
        get() = getByValue(PermissionEnum.RESUME_MEDIA)

    val UPDATE_POSITION
        get() = getByValue(PermissionEnum.UPDATE_POSITION_MEDIA)

    val CLEAR_QUEUE
        get() = getByValue(PermissionEnum.CLEAR_QUEUE)

    val CREATE_CATEGORY
        get() = getByValue(PermissionEnum.CREATE_CATEGORY)

    val DELETE_CATEGORY
        get() = getByValue(PermissionEnum.DELETE_CATEGORY)

    @Throws(InitialPermissionNotFoundException::class)
    fun getByValue(permission: PermissionEnum) = permissionRepository.findByValue(permission.value)
            ?: throw InitialPermissionNotFoundException(permission.value)

    fun getByValue(value: String) = permissionRepository.findByValue(value)

    fun getDefaultCreateServerPermissions(): List<Permission>? {
        return try {
            listOf(
                    CREATE_CONTENT,
                    DELETE_CONTENT,
                    READ_CONTENT,
                    CHANGE_MODE,
                    PLAY_MEDIA,
                    STOP_MEDIA,
                    PAUSE_MEDIA,
                    RESUME_MEDIA,
                    UPDATE_POSITION,
                    CLEAR_QUEUE,
                    CREATE_CATEGORY,
                    DELETE_CATEGORY
            ).requireNoNulls()
        } catch (e: Exception) {
            null
        }
    }

    fun getDefaultJoinServerPermissions(): List<Permission>? {
        return try {
            listOf(
                    CREATE_CONTENT,
                    DELETE_CONTENT,
                    READ_CONTENT,
                    CHANGE_MODE,
                    PLAY_MEDIA,
                    STOP_MEDIA,
                    PAUSE_MEDIA,
                    RESUME_MEDIA,
                    UPDATE_POSITION,
                    CLEAR_QUEUE,
                    CREATE_CATEGORY,
                    DELETE_CATEGORY
            ).requireNoNulls()
        } catch (e: Exception) {
            null
        }
    }
}