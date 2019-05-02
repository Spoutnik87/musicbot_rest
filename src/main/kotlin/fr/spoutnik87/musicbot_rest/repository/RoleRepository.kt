package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByUuid(uuid: String): Role?
    fun findByName(name: String): Role?
}