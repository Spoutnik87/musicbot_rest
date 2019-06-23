package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "LocalMetadata")
data class LocalMetadata(
        @Column(nullable = false)
        var mediaSize: Long,
        @ManyToOne
        @JoinColumn(name = "mime_type_id")
        var mimeType: MimeType
) : AuditModel(), Serializable {

        @OneToOne(mappedBy = "localMetadata")
        var content: Content? = null
}