package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "UserTable")
data class User(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false, unique = true)
        var email: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false, unique = true)
        var nickname: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var firstname: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var lastname: String,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Column(nullable = false)
        var password: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @ManyToOne
    @JoinColumn(name = "role_id")
    @JsonManagedReference
    lateinit var role: Role

    constructor(uuid: String, email: String, nickname: String, firstname: String, lastname: String, password: String, role: Role) : this(uuid, email, nickname, firstname, lastname, password) {
        this.role = role
    }

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    @JsonBackReference
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL])
    @JsonBackReference
    val serverSet: MutableSet<Server> = HashSet()

    val groupList
        get() = userGroupSet.map { it.group }

    /**
     * Return true if this user is the owner of the specified server.
     */
    fun isOwner(server: Server) = serverSet.any { it.id == server.id }

    /**
     * Return true if this user is a member of the specified server.
     */
    fun hasServer(server: Server) = userGroupSet.map { it.group.server }.any { it.id == server.id }

    fun hasPermission(group: Group, permission: Permission) = userGroupSet.filter { it.group.id == group.id }.any { it.hasPermission(permission) }

    fun hasPermission(group: Group, permissionEnum: PermissionEnum) = userGroupSet.filter { it.group.id == group.id }.any { it.hasPermission(permissionEnum) }

    fun hasReadMediaPermission(media: Media) = media.groupList.any { hasPermission(it, PermissionEnum.READ_MEDIA) }

    fun hasDeleteMediaPermission(media: Media) = media.groupList.any { hasPermission(it, PermissionEnum.DELETE_MEDIA) }

    fun hasCreateMediaPermission(group: Group) = groupList.filter { it.id == group.id }.any { hasPermission(it, PermissionEnum.CREATE_MEDIA) }

    fun hasCreateMediaPermission(media: Media) = media.groupList.any { hasPermission(it, PermissionEnum.CREATE_MEDIA) }

    fun hasCreateCategoryPermission(server: Server) = server.groupSet.any { hasPermission(it, PermissionEnum.CREATE_CATEGORY) }

    fun hasCreateCategoryPermission(category: Category) = hasCreateCategoryPermission(category.server)

    fun hasDeleteCategoryPermission(category: Category) = category.server.groupSet.any { hasPermission(it, PermissionEnum.DELETE_CATEGORY) }
}