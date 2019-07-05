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
        var password: String,
        @Column(nullable = false)
        var thumbnailSize: Long
) : AuditModel(), Serializable {

    /**
     * Discord user unique Id
     */
    var userId: String? = null

    @ManyToOne
    @JoinColumn(name = "group_id")
    lateinit var group: Group

    @ManyToOne
    @JoinColumn(name = "role_id")
    lateinit var role: Role

    constructor(uuid: String, email: String, nickname: String, firstname: String, lastname: String, password: String, role: Role, thumbnailSize: Long) : this(uuid, email, nickname, firstname, lastname, password, thumbnailSize) {
        this.role = role
    }

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val userGroupSet: MutableSet<UserGroup> = HashSet()

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL])
    val ownedServerSet: MutableSet<Server> = HashSet()

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL])
    val createdContentSet: MutableSet<Content> = HashSet()

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL])
    val createdCategorySet: MutableSet<Category> = HashSet()

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL])
    val createdGroupSet: MutableSet<Group> = HashSet()

    val groupList
        get() = userGroupSet.map { it.group }.distinctBy { it.id }

    val isLinked
        get() = userId != null

    val serverList
        get() = groupList.map { it.server }.distinctBy { it.id }

    val spaceUsed
        get() = thumbnailSize + createdContentSet.map { it.spaceUsed }.reduce { acc, l -> acc + l }

    val ownedServerCount
        get() = ownedServerSet.size

    val createdContentCount
        get() = createdContentSet.size

    val createdCategoryCount
        get() = createdCategorySet.size

    val createdGroupCount
        get() = createdGroupSet.size

    /**
     * Return true if this user is the owner of the specified server.
     */
    fun isOwner(server: Server) = ownedServerSet.any { it.id == server.id }

    /**
     * Return true if this user created the specified content.
     */
    fun isAuthor(content: Content) = createdContentSet.any { it.id == content.id }

    /**
     * Return true if this user created the specified category.
     */
    fun isAuthor(category: Category) = createdCategorySet.any { it.id == category.id }

    /**
     * Return true if this user created the specified group.
     */
    fun isAuthor(group: Group) = createdGroupSet.any { it.id == group.id }

    /**
     * Return true if this user is a member of the specified server.
     */
    fun hasServer(server: Server) = serverList.any { it.id == server.id }

    fun hasThumbnail() = thumbnailSize > 0

    /**
     * Get accessible contents.
     */
    fun getVisibleContents(server: Server) = groupList.filter { it.server.id == server.id }.flatMap { it.visibleContentList }.distinctBy { it.id }

    /**
     * Get user permissions in the specified Server.
     */
    fun getPermissions(server: Server) = groupList.filter { it.server.id == server.id }.flatMap { it.permissionSet }.distinctBy { it.id }

    /**
     * Get user permissions in the specified Group.
     */
    fun getPermissions(group: Group) = groupList.filter { it.id == group.id }.flatMap { it.permissionSet }

    fun hasPermission(group: Group, permission: Permission) = getPermissions(group).any { it.id == permission.id }

    fun hasPermission(group: Group, permissionEnum: PermissionEnum) = getPermissions(group).any { it.value == permissionEnum.value }

    fun hasPermission(server: Server, permission: Permission) = getPermissions(server).any { it.id == permission.id }

    fun hasPermission(server: Server, permissionEnum: PermissionEnum) = getPermissions(server).any { it.value == permissionEnum.value }

    fun hasReadContentPermission(server: Server) = hasPermission(server, PermissionEnum.READ_CONTENT)

    fun hasDeleteContentPermission(server: Server) = hasPermission(server, PermissionEnum.DELETE_CONTENT)

    fun hasCreateContentPermission(server: Server) = hasPermission(server, PermissionEnum.CREATE_CONTENT)

    fun hasCreateCategoryPermission(server: Server) = hasPermission(server, PermissionEnum.CREATE_CATEGORY)

    fun hasDeleteCategoryPermission(server: Server) = hasPermission(server, PermissionEnum.DELETE_CATEGORY)

    fun hasPlayMediaPermission(server: Server) = hasPermission(server, PermissionEnum.PLAY_MEDIA)

    fun hasStopMediaPermission(server: Server) = hasPermission(server, PermissionEnum.STOP_MEDIA)

    fun hasPauseMediaPermission(server: Server) = hasPermission(server, PermissionEnum.PAUSE_MEDIA)

    fun hasResumeMediaPermission(server: Server) = hasPermission(server, PermissionEnum.RESUME_MEDIA)

    fun hasUpdatePositionMediaPermission(server: Server) = hasPermission(server, PermissionEnum.UPDATE_POSITION_MEDIA)

    /**
     * Return true if this user is allowed to clear @param server queue.
     */
    fun hasClearQueuePermission(server: Server) = hasPermission(server, PermissionEnum.CLEAR_QUEUE)
}