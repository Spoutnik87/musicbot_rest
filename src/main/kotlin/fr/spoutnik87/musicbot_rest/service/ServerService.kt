package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.*
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import fr.spoutnik87.musicbot_rest.repository.ServerRepository
import fr.spoutnik87.musicbot_rest.repository.UserGroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedInputStream

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

    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var fileService: FileService

    @Transactional
    fun create(name: String, owner: User, permissions: List<Permission>): Server? {
        if (!validName(name)) {
            return null
        }
        val serverUuid = uuid.v4()
        val groupUuid = uuid.v4()
        val serverThumbnail = imageService.generateRandomImage(serverUuid)
        val groupThumbnail = imageService.generateRandomImage(groupUuid)
        var server = Server(serverUuid, name, serverThumbnail.size.toLong(), owner, owner)
        var group = Group(groupUuid, "Default", groupThumbnail.size.toLong())
        group.author = owner
        group.permissionSet = permissions.toMutableSet()
        server.defaultGroup = group
        server = serverRepository.save(server)
        group = server.defaultGroup
        group.server = server
        var userGroup = UserGroup(owner, group)
        userGroupRepository.save(userGroup)
        groupRepository.save(group)
        fileService.saveFile(appConfig.serverThumbnailsPath + server.uuid, serverThumbnail)
        fileService.saveFile(appConfig.groupThumbnailsPath + group.uuid, groupThumbnail)
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

    @Transactional
    fun updateThumbnail(server: Server, inputStream: BufferedInputStream): Server? {
        if (!fileService.isImage(inputStream)) {
            return null
        }
        if (server.hasThumbnail()) {
            fileService.deleteFile(appConfig.categoryThumbnailsPath + server.uuid)
            server.thumbnailSize = 0
            serverRepository.save(server)
        }
        val resizedThumbnail = try {
            val resized = imageService.resize(inputStream.readBytes(), 400, 400)
            inputStream.close()
            resized
        } catch (e: Exception) {
            inputStream.close()
            return null
        }
        fileService.saveFile(appConfig.serverThumbnailsPath + server.uuid, resizedThumbnail)
        server.thumbnailSize = resizedThumbnail.size.toLong()
        return serverRepository.save(server)
    }

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validName(name: String?) = name != null && name.length <= 255
}