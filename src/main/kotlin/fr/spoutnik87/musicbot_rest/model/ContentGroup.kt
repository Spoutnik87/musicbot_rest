package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "ContentGroup")
data class ContentGroup(
        @Column(nullable = false, unique = true)
        var uuid: String
) : AuditModel(), Serializable {

    @ManyToOne
    @JoinColumn(name = "content_id")
    lateinit var content: Content

    @ManyToOne
    @JoinColumn(name = "group_id")
    lateinit var group: Group

    constructor(uuid: String, content: Content, group: Group) : this(uuid) {
        this.content = content
        this.group = group
    }
}