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

    fun create(email: String, nickname: String, firstname: String, lastname: String, password: String, role: Role): User? {
        if (!validEmail(email) || !validNickname(nickname) || !validFirstname(firstname) || !validLastname(lastname) || !validPassword(password)) {
            return null
        }
        return userRepository.save(User(uuid.v4(), email, nickname, firstname, lastname, bCryptPasswordEncoder.encode(password), role))
    }

    fun update(user: User, nickname: String?, firstname: String?, lastname: String?, password: String?): User? {
        var updated = false
        if (validNickname(nickname)) {
            user.nickname = nickname!!
            updated = true
        }
        if (validFirstname(firstname)) {
            user.firstname = firstname!!
            updated = true
        }
        if (validLastname(lastname)) {
            user.lastname = lastname!!
            updated = true
        }
        if (validPassword(password)) {
            user.password = bCryptPasswordEncoder.encode(password)
            updated = true
        }
        return if (updated) {
            userRepository.save(user)
        } else {
            null
        }
    }

    /**
     * Return true if email is valid.
     * Max length is 255 characters.
     */
    private fun validEmail(email: String?) = email != null && email.length <= 255


    /**
     * Return true if nickname is valid.
     * Max length is 255 characters.
     */
    private fun validNickname(nickname: String?) = nickname != null && nickname.length <= 255

    /**
     * Return true if name is valid.
     * Max length is 255 characters.
     */
    private fun validFirstname(firstname: String?) = firstname != null && firstname.length <= 255

    /**
     * Return true if lastname is valid.
     * Max length is 255 characters.
     */
    private fun validLastname(lastname: String?) = lastname != null && lastname.length <= 255

    /**
     * Return true if password is valid.
     * Max length is 255 characters.
     */
    private fun validPassword(password: String?) = password != null && password.length <= 255
}