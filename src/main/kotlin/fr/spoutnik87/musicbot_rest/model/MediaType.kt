package fr.spoutnik87.musicbot_rest.model

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "MediaType")
data class MediaType(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false, unique = true)
        var value: String
) : AuditModel(), Serializable {

    @OneToMany(mappedBy = "mediaType", cascade = [CascadeType.ALL])
    val mediaSet: MutableSet<Media> = HashSet()
}