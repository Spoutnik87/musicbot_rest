package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "MediaType")
data class MediaType(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false, unique = true)
        var value: String
) : AuditModel(), Serializable {

    @JsonView(Views.Companion.Public::class)
    @OneToMany(mappedBy = "mediaType", cascade = [CascadeType.ALL])
    @JsonBackReference
    val mediaSet: MutableSet<Media> = HashSet()
}