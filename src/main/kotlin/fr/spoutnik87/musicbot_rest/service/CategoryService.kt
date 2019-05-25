package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Category
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.repository.CategoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CategoryService {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var uuid: UUID

    fun create(name: String, server: Server): Category? {
        if (!validName(name)) {
            return null
        }
        return categoryRepository.save(Category(uuid.v4(), name, server))
    }

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

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validName(name: String?) = name != null && name.length <= 255
}