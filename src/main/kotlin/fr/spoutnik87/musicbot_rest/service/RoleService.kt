package fr.spoutnik87.musicbot_rest.service

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.exception.InitialRoleNotFoundException
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RoleService {

    @Autowired
    private lateinit var roleRepository: RoleRepository

    val allRoles: List<Role>
        get() = roleRepository.findAll()

    val allInitialRoles
        get() = RoleEnum.values().map { getByValue(it) }

    val ADMIN
        get() = getByValue(RoleEnum.ADMIN)

    val USER
        get() = getByValue(RoleEnum.USER)

    val BOT
        get() = getByValue(RoleEnum.BOT)

    fun getByValue(value: String) = roleRepository.findByName(value)

    @Throws(InitialRoleNotFoundException::class)
    fun getByValue(role: RoleEnum) = roleRepository.findByName(role.value)
            ?: throw InitialRoleNotFoundException(role.value)
}