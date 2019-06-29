package fr.spoutnik87.musicbot_rest.repository

import fr.spoutnik87.musicbot_rest.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): User?
    @Transactional(readOnly = true)
    fun findByEmail(email: String): User?
    @Transactional(readOnly = true)
    fun findByNickname(nickname: String): User?
    @Transactional(readOnly = true)
    fun findByUserId(userId: String): User?
}