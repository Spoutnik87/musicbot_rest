package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Group
import fr.spoutnik87.musicbot_rest.model.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface GroupRepository : JpaRepository<Group, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Group?
    @Transactional(readOnly = true)
    fun findByUuidAndServer(uuid: String, server: Server): Group?
}