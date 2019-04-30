package fr.spoutnik87.musicbot_rest.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "Bot")
data class Bot(
        @JsonView(Views.Companion.Public::class)
        @JsonProperty("id")
        @Column(nullable = false, unique = true)
        var uuid: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var name: String,
        @JsonView(Views.Companion.Public::class)
        @Column(nullable = false)
        var token: String
) : AuditModel(), Serializable {
    @JsonView(Views.Companion.Public::class)
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "server_id")
    @JsonManagedReference
    lateinit var server: Server

    constructor(uuid: String, name: String, token: String, server: Server) : this(uuid, name, token) {
        this.server = server
    }
}