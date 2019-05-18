package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonProperty
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "UserTable")
data class User(
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false, unique = true)
        var email: String,
        @Column(nullable = false, unique = true)
        var nickname: String,
        @Column(nullable = false)
        var firstname: String,
        @Column(nullable = false)
        var lastname: String,
        @Column(nullable = false)
        var password: String
) : AuditModel(), Serializable {

    /**
     * Discord user unique Id
     */
    var userId: String? = null

    @ManyToOne
    @JoinColumn(name = "role_id")
    lateinit var role: Role

    constructor(uuid: String, email: String, nickname: String, firstname: String, lastname: String, password: String, role: Role) : this(uuid, email, nickname, firstname, lastname, password) {
        this.role = role
    }

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL])
    val ownedServerSet: MutableSet<Server> = HashSet()

    val groupList
        get() = userGroupSet.map { it.group }

    val isLinked
        get() = userId != null

    val servers
        get() = userGroupSet.map { it.group.server }

    /**
     * Return true if this user is the owner of the specified server.
     */
    fun isOwner(server: Server) = ownedServerSet.any { it.id == server.id }

    /**
     * Return true if this user is a member of the specified server.
     */
    fun hasServer(server: Server) = servers.any { it.id == server.id }

    fun getPermissions(group: Group) = userGroupSet.filter { it.group.id == group.id }.flatMap { it.permissionSet }

    fun hasPermission(group: Group, permission: Permission) = userGroupSet.filter { it.group.id == group.id }.any { it.hasPermission(permission) }

    fun hasPermission(group: Group, permissionEnum: PermissionEnum) = userGroupSet.filter { it.group.id == group.id }.any { it.hasPermission(permissionEnum) }

    fun hasReadContentPermission(content: Content) = content.groupList.any { hasPermission(it, PermissionEnum.READ_CONTENT) }

    fun hasDeleteContentPermission(content: Content) = content.groupList.any { hasPermission(it, PermissionEnum.DELETE_CONTENT) }

    fun hasCreateContentPermission(group: Group) = groupList.filter { it.id == group.id }.any { hasPermission(it, PermissionEnum.CREATE_CONTENT) }

    fun hasCreateContentPermission(content: Content) = content.groupList.any { hasPermission(it, PermissionEnum.CREATE_CONTENT) }

    fun hasCreateCategoryPermission(server: Server) = server.groupSet.any { hasPermission(it, PermissionEnum.CREATE_CATEGORY) }

    fun hasCreateCategoryPermission(category: Category) = hasCreateCategoryPermission(category.server)

    fun hasDeleteCategoryPermission(category: Category) = category.server.groupSet.any { hasPermission(it, PermissionEnum.DELETE_CATEGORY) }

    fun hasPlayMediaPermission(content: Content) = content.contentGroupSet.flatMap { it.group.userGroupSet }.filter { it.user.id == id }.any { it.hasPermission(PermissionEnum.PLAY_MEDIA) }

    fun hasStopMediaPermission(content: Content) = content.contentGroupSet.flatMap { it.group.userGroupSet }.filter { it.user.id == id }.any { it.hasPermission(PermissionEnum.STOP_MEDIA) }
}