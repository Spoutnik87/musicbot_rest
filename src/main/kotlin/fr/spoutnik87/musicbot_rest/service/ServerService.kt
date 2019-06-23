package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserGroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServerService {

    @Autowired
    private lateinit var serverRepository: ServerRepository

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var uuid: UUID

    @Transactional
    fun create(name: String, owner: User, permissions: List<Permission>): Server? {
        if (!validName(name)) {
            return null
        }
        var group = Group(uuid.v4(),"Default", 0)
        group.author = owner
        /**
         * TODO Generate thumbnail
         */
        var server = Server(uuid.v4(), name, 0, owner, owner)
        server.defaultGroup = group
        /**
         * Persist group and server
         */
        server = serverRepository.save(server)
        group = server.defaultGroup
        /**
         * Link group to server and persist
         */
        group.server = server
        group.permissionSet = permissions.toMutableSet()
        groupRepository.save(group)
        var userGroup = UserGroup(owner, group)
        userGroupRepository.save(userGroup)
        return server
    }

    @Transactional
    fun update(server: Server, name: String?): Server? {
        var updated = false
        if (validName(name)) {
            server.name = name!!
            updated = true
        }
        return if (updated) {
            serverRepository.save(server)
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