package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.ContentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContentGroupRepository : JpaRepository<ContentGroup, Long> {
    fun findByUuid(uuid: String): ContentGroup?
}