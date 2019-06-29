package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Category?
}