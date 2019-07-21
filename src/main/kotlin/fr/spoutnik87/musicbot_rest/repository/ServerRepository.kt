package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ServerRepository : JpaRepository<Server, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Server?
    @Transactional(readOnly = true)
    fun findByGuildId(guildId: String): Server?
}