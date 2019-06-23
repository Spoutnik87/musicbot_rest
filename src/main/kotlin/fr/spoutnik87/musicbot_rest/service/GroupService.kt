package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var permissionService: PermissionService

    @Autowired
    private lateinit var uuid: UUID

    @Transactional
    fun create(name: String, author: User, server: Server, permissions: List<Permission> = permissionService.allInitialPermissions): Group? {
        if (!validName(name)) {
            return null
        }
        /**
         * TODO Generate thumbnail
         */
        return groupRepository.save(Group(uuid.v4(), name, 0, author, server, permissions))
    }

    @Transactional
    fun update(group: Group, name: String?): Group? {
        var updated = false
        if (validName(name)) {
            group.name = name!!
            updated = true
        }
        return if (updated) {
            groupRepository.save(group)
        } else {
            null
        }
    }

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validName(name: String?) = name != null && name.length <= 255
}