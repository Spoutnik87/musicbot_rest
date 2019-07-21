package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.AppConfig
import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Category
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.CategoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedInputStream

@Service
class CategoryService {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var fileService: FileService

    @Transactional
    fun create(name: String, author: User, server: Server): Category? {
        if (!validName(name)) {
            return null
        }
        val uuid = uuid.v4()
        val thumbnail = imageService.generateRandomImage(uuid)
        fileService.saveFile(appConfig.categoryThumbnailsPath + uuid, thumbnail)
        return categoryRepository.save(Category(uuid, name, thumbnail.size.toLong(), author, server))
    }

    @Transactional
    fun update(category: Category, name: String?): Category? {
        var updated = false
        if (validName(name)) {
            category.name = name!!
            updated = true
        }
        return if (updated) {
            categoryRepository.save(category)
        } else {
            null
        }
    }

    @Transactional
    fun updateThumbnail(category: Category, inputStream: BufferedInputStream): Category? {
        if (!fileService.isImage(inputStream)) {
            return null
        }
        if (category.hasThumbnail()) {
            fileService.deleteFile(appConfig.categoryThumbnailsPath + category.uuid)
            category.thumbnailSize = 0
            categoryRepository.save(category)
        }
        val resizedThumbnail = try {
            val resized = imageService.resize(inputStream.readBytes(), 400, 400)
            inputStream.close()
            resized
        } catch (e: Exception) {
            inputStream.close()
            return null
        }
        fileService.saveFile(appConfig.categoryThumbnailsPath + category.uuid, resizedThumbnail)
        category.thumbnailSize = resizedThumbnail.size.toLong()
        return categoryRepository.save(category)
    }

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validName(name: String?) = name != null && name.length <= 255
}