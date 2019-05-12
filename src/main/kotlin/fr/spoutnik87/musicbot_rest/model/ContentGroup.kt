package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "ContentGroup")
data class ContentGroup(
        @ManyToOne
        @JoinColumn(name = "content_id")
        var content: Content,
        @ManyToOne
        @JoinColumn(name = "group_id")
        var group: Group
) : AuditModel(), Serializable {
}