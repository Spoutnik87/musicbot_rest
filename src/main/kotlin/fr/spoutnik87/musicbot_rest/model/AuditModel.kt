package fr.spoutnik87.musicbot_rest.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditModel : Serializable {

    /**
     * Generate a temporary ID. It is overwritten when saved in the repository.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = System.nanoTime()

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Long = 0

    @LastModifiedDate
    val updatedAt: Long = 0
}