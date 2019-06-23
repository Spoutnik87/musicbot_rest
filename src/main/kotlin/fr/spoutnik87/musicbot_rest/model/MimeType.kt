package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "MimeType")
data class MimeType(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false)
        var value: String
): AuditModel(), Serializable {

        @OneToMany(mappedBy = "mimeType", cascade = [CascadeType.ALL])
        val localMetadataList: MutableSet<LocalMetadata> = HashSet()

        val contents
                get() = localMetadataList.mapNotNull { it.content }.distinctBy { it.id }
}