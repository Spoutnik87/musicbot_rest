package fr.spoutnik87.musicbot_rest.model

import fr.spoutnik87.musicbot_rest.constant.PermissionEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "GroupTable")
data class Group(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String,
        @Column(nullable = false)
        var thumbnailSize: Long
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "author_id")
    lateinit var author: User

    @ManyToOne
    @JoinColumn(name = "server_id")
    lateinit var server: Server

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "user_group_permission", joinColumns = [JoinColumn(name = "group_server_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")])
    var permissionSet: MutableSet<Permission> = HashSet()

    constructor(uuid: String, name: String, thumbnailSize: Long, user: User, server: Server, permissions: List<Permission>) : this(uuid, name, thumbnailSize) {
        this.author = user
        this.server = server
        this.permissionSet.addAll(permissions)
    }

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    val contentGroupSet: MutableSet<ContentGroup> = HashSet()

    val userList
        get() = userGroupSet.map { it.user }

    val contentList
        get() = contentGroupSet.map { it.content }

    val visibleContentList
        get() = contentGroupSet.filter { it.visible }.map { it.content }

    fun hasUser(user: User) = userGroupSet.any { it.user.id == user.id }

    fun hasContent(content: Content) = contentGroupSet.filter { it.visible }.any { it.content.id == content.id }

    fun hasPermission(permission: Permission) = permissionSet.any { it.id == permission.id }

    fun hasPermission(permissionEnum: PermissionEnum) = permissionSet.any { it.value == permissionEnum.value }

    fun hasThumbnail() = thumbnailSize > 0
}