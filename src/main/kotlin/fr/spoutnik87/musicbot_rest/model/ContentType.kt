package fr.spoutnik87.musicbot_rest.model

import fr.spoutnik87.musicbot_rest.constant.ContentTypeEnum
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "ContentType")
data class ContentType(
        @Column(nullable = false, unique = true)
        var uuid: String,
        @Column(nullable = false, unique = true)
        var value: String
) : AuditModel(), Serializable {

    @OneToMany(mappedBy = "contentType", cascade = [CascadeType.ALL])
    val contentSet: MutableSet<Content> = HashSet()

    val toEnum
        get() = ContentTypeEnum.valueOf(value)
}