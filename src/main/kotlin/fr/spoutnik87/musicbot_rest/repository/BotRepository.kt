package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Bot
import fr.spoutnik87.musicbot_rest.model.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BotRepository : JpaRepository<Bot, Long> {
    fun findByUuid(uuid: String): Bot?
    fun findByServer(server: Server): Bot?
}