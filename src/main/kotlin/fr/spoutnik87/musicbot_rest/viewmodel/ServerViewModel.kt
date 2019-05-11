package fr.spoutnik87.musicbot_rest.viewmodel

import com.fasterxml.jackson.annotation.JsonView
import fr.spoutnik87.musicbot_rest.model.Server
import fr.spoutnik87.musicbot_rest.model.Views


data class ServerViewModel(
        @JsonView(Views.Companion.Public::class)
        val id: String,
        @JsonView(Views.Companion.Public::class)
        val name: String,
        @JsonView(Views.Companion.Public::class)
        val ownerId: String,
        @JsonView(Views.Companion.Private::class)
        val linkToken: String?,
        @JsonView(Views.Companion.Public::class)
        val linked: Boolean

) {

    companion object {

        fun from(server: Server) = ServerViewModel(server.uuid, server.name, server.owner.uuid, server.linkToken, server.isLinked)
    }
}