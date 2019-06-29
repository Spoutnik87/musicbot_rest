package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Role?
    @Transactional(readOnly = true)
    fun findByName(name: String): Role?
}