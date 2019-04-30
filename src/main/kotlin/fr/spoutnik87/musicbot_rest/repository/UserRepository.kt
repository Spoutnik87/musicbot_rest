package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUuid(uuid: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun findByNickname(nickname: String): Optional<User>
}