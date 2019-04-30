package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.security.UserDetails
import fr.spoutnik87.musicbot_rest.security.UserDetailsServiceImpl
import org.springframework.security.core.userdetails.UsernameNotFoundException

class InMemoryUserDetailsManager : UserDetailsServiceImpl {

    private var userDetailsMap: Map<String, UserDetails> = HashMap()

    constructor(userDetailsList: List<UserDetails>) {
        userDetailsMap = userDetailsList.associateBy({ it.username }, { it })
    }

    fun createUser(userDetails: UserDetails) {
        if (userDetailsMap.containsKey(userDetails.username)) {
            return
        }
        userDetailsMap.plus(Pair(userDetails.username, userDetails))
    }

    fun deleteUser(username: String) {
        userDetailsMap.minus(username)
    }

    fun userExists(username: String) = userDetailsMap.containsKey(username)

    override fun loadUserByUsername(username: String): UserDetails = userDetailsMap?.get(username)
            ?: throw UsernameNotFoundException(username)
}