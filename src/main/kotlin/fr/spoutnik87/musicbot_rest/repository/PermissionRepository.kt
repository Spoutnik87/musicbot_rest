package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, Long> {
    fun findByUuid(uuid: String): Permission?
    fun findByValue(value: String): Permission?
}