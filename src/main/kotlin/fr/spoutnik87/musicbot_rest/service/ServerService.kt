package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.model.UserGroup
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
        val uuid = uuid.v4()
        val thumbnail = imageService.generateRandomImage(uuid)
        var server = Server(uuid, name, thumbnail.size.toLong(), owner, owner)
        val group = groupService.create("Default", owner, server, permissions) ?: return null
        fileService.saveFile(appConfig.serverThumbnailsPath + server.uuid, thumbnail)
        server.defaultGroup = group
        var userGroup = UserGroup(owner, group)
        userGroupRepository.save(userGroup)
        return serverRepository.save(server)
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