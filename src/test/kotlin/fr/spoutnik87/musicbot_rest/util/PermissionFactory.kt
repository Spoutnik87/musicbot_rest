package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Permission

class PermissionFactory {

    private var permission: Permission = createDefault().build()

    fun create(uuid: String, value: String): PermissionFactory {
        permission = Permission(uuid, value)
        return this
    }

    fun create(permissionEnum: PermissionEnum): PermissionFactory {
        permission = Permission(getUuid(permissionEnum), permissionEnum.value)
        return this
    }

    fun addGroup(group: Group): PermissionFactory {
        permission.groupSet.add(group)
        return this
    }

    fun clearGroups(): PermissionFactory {
        permission.groupSet.clear()
        return this
    }

    fun createDefault() = create(PermissionEnum.CREATE_CONTENT)

    fun build() = permission

    private fun getUuid(permissionEnum: PermissionEnum): String {
        return when(permissionEnum) {
            PermissionEnum.CREATE_CONTENT -> "createContentToken"
            PermissionEnum.DELETE_CONTENT -> "deleteContentToken"
            PermissionEnum.READ_CONTENT -> "readContentToken"
            PermissionEnum.CHANGE_MODE -> "changeModeToken"
            PermissionEnum.PLAY_MEDIA -> "playMediaToken"
            PermissionEnum.STOP_MEDIA -> "stopMediaToken"
            PermissionEnum.PAUSE_MEDIA -> "pauseMediaToken"
            PermissionEnum.RESUME_MEDIA -> "resumeMediaToken"
            PermissionEnum.UPDATE_POSITION_MEDIA -> "updatePositionMediaToken"
            PermissionEnum.CLEAR_QUEUE -> "clearQueueToken"
            PermissionEnum.CREATE_CATEGORY -> "createCategoryToken"
            PermissionEnum.DELETE_CATEGORY -> "deleteCategoryToken"
        }
    }
}