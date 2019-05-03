package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Category")
data class Category(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var name: String
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "server_id")
    lateinit var server: Server

    constructor(uuid: String, name: String, server: Server) : this(uuid, name) {
        this.server = server
    }

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL])
    val mediaSet: MutableSet<Media> = HashSet()
}