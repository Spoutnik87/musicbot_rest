package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PermissionRepository : JpaRepository<Permission, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Permission?
    @Transactional(readOnly = true)
    fun findByValue(value: String): Permission?
}