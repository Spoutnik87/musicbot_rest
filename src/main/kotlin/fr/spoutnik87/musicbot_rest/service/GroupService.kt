package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GroupService {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var uuid: UUID

    fun create(name: String, server: Server): Group? {
        if (!validName(name)) {
            return null
        }
        return groupRepository.save(Group(uuid.v4(), name, server))
    }

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