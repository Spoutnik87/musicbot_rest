package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.Bot
import fr.spoutnik87.musicbot_rest.model.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BotRepository : JpaRepository<Bot, Long> {
    fun findByUuid(uuid: String): Optional<Bot>
    fun findByServer(server: Server): Optional<Bot>
}