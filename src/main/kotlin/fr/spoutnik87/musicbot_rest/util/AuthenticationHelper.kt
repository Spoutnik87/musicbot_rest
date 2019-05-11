package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.security.UserDetails
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

abstract class AuthenticationHelper {

    companion object {
        /**
         * Return true if an user is authenticated, false if not.
         *
         * @return Authentication status.
         */
        fun isAuthenticated() = SecurityContextHolder.getContext().authentication != null

        fun isAuthenticatedUserInRole(role: RoleEnum) = getAuthenticatedUserAuthorities()?.any { it.authority == role.value }
                ?: false

        /**
         * Return informations about connected user or null if no user is connected.
         *
         * @return User informations
         */
        fun getAuthenticatedUserDetails(): UserDetails? = SecurityContextHolder.getContext().authentication?.principal as UserDetails?

        /**
         * Return email of connected user or null if no user is connected.
         *
         * @return User email
         */
        fun getAuthenticatedUserEmail(): String? = getAuthenticatedUserDetails()?.username

        fun getAuthenticatedUserAuthorities() = getAuthenticatedUserDetails()?.authorities?.map { it as SimpleGrantedAuthority }

        /**
         * Return User model instance of authenticated user.
         */
        fun getAuthenticatedUser(): User? = getAuthenticatedUserDetails()?.user
    }
}