package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.repository.UserRepository
import fr.spoutnik87.musicbot_rest.security.UserDetails
import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException

class InMemoryUserDetailsManager : UserDetailsServiceImpl {

    private var userDetailsMap: MutableMap<String, UserDetails> = HashMap()
    private lateinit var userRepository: UserRepository

    constructor(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    constructor(userDetailsList: List<UserDetails>) {
        userDetailsMap = userDetailsList.associateBy({ it.username }, { it }).toMutableMap()
    }

    fun createUser(userDetails: UserDetails) {
        if (userDetailsMap.containsKey(userDetails.username)) {
            return
        }
        userDetailsMap[userDetails.username] = userDetails
    }

    fun deleteUser(username: String) {
        userDetailsMap.remove(username)
    }

    fun deleteAllUsers() {
        userDetailsMap.clear()
    }

    fun userExists(username: String) = userDetailsMap.containsKey(username)

    /**
     * Return a complete copy of desired UserDetails object.
     */
    override fun loadUserByUsername(username: String): UserDetails {
        var user = userRepository.findByEmail(username) ?: throw UsernameNotFoundException(username)
        return UserDetails(user, listOf(SimpleGrantedAuthority(user.role.name)))
        // return userDetailsMap?.get(username) ?: throw UsernameNotFoundException(username)
        /*var userDetails = userDetailsMap?.get(username)?.copy() ?: throw UsernameNotFoundException(username)
        userDetails.user = userDetails.user.copy()
        userDetails.user.role = userDetails
        return userDetails*/
    }
}