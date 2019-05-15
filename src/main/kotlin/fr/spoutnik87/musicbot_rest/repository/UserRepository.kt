package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUuid(uuid: String): User?
    fun findByEmail(email: String): User?
    fun findByNickname(nickname: String): User?
    fun findByUserId(userId: String): User?
}