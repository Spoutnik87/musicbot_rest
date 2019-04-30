package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.UserGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupRepository : JpaRepository<UserGroup, Long> {
    fun findByUuid(uuid: String): Optional<UserGroup>
}