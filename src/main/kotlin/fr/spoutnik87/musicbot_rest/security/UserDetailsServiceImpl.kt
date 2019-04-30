package fr.spoutnik87.musicbot_rest.security

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl : UserDetailsService {
    @Autowired
    private lateinit var userRepository: UserRepository

    override fun loadUserByUsername(email: String): UserDetails {
        val optionalUser = userRepository.findByEmail(email)
        if (!optionalUser.isPresent) {
            throw UsernameNotFoundException(email)
        }
        val user = optionalUser.get()
        return UserDetails(user, getAuthorities(user.role.lvl))
    }

    private fun getAuthorities(lvl: Int): Collection<GrantedAuthority> {
        val authorities = ArrayList<GrantedAuthority>()
        when (lvl) {
            RoleEnum.ADMIN.lvl -> authorities.add(SimpleGrantedAuthority(RoleEnum.ADMIN.value))
            RoleEnum.USER.lvl -> authorities.add(SimpleGrantedAuthority(RoleEnum.USER.value))
        }
        return authorities
    }
}