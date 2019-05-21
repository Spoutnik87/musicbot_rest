package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.UUID
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.util.AuthenticationHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var uuid: UUID

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    fun getAuthenticatedUser(): User? {
        val email = AuthenticationHelper.getAuthenticatedUserEmail() ?: return null
        return userRepository.findByEmail(email)
    }

    fun save(email: String, nickname: String, firstname: String, lastname: String, password: String, role: Role): User {
        return userRepository.save(User(uuid.v4(), email, nickname, firstname, lastname, bCryptPasswordEncoder.encode(password), role))
    }
}