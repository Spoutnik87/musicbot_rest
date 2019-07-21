package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Permission
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.GroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedInputStream

@Service
class GroupService {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Autowired
    private lateinit var permissionService: PermissionService

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var fileService: FileService

    @Transactional
    fun create(name: String, author: User, server: Server, permissions: List<Permission> = permissionService.allInitialPermissions): Group? {
        if (!validName(name)) {
            return null
        }
        val uuid = uuid.v4()
        val thumbnail = imageService.generateRandomImage(uuid)
        fileService.saveFile(appConfig.groupThumbnailsPath + uuid, thumbnail)
        return groupRepository.save(Group(uuid, name, thumbnail.size.toLong(), author, server, permissions))
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

    @Transactional
    fun updateThumbnail(group: Group, inputStream: BufferedInputStream): Group? {
        if (!fileService.isImage(inputStream)) {
            return null
        }
        if (group.hasThumbnail()) {
            fileService.deleteFile(appConfig.groupThumbnailsPath + group.uuid)
            group.thumbnailSize = 0
            groupRepository.save(group)
        }
        val resizedThumbnail = try {
            val resized = imageService.resize(inputStream.readBytes(), 400, 400)
            inputStream.close()
            resized
        } catch (e: Exception) {
            inputStream.close()
            return null
        }
        fileService.saveFile(appConfig.groupThumbnailsPath + group.uuid, resizedThumbnail)
        group.thumbnailSize = resizedThumbnail.size.toLong()
        return groupRepository.save(group)
    }

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validName(name: String?) = name != null && name.length <= 255
}