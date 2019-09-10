package fr.spoutnik87.musicbot_rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import fr.spoutnik87.musicbot_rest.service.ContentPlayerGetStateMessage
import fr.spoutnik87.musicbot_rest.service.ContentPlayerJoinMessage
import fr.spoutnik87.musicbot_rest.service.ContentPlayerLeaveMessage
import fr.spoutnik87.musicbot_rest.service.WebSocketPlayerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler

enum class WebSocketActionType(
        val value: String
) {
    CONNECT_PLAYER("CONNECT_PLAYER"),
    DISCONNECT_PLAYER("DISCONNECT_PLAYER"),
    GET_PLAYER_STATE("GET_PLAYER_STATE"),
}

data class WebSocketAction(val action: WebSocketActionType)

@Component
class WebSocketPlayerHandler : AbstractWebSocketHandler() {

    @Autowired
    private lateinit var webSocketPlayerService: WebSocketPlayerService

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        handlePlayerAction(message.payload, session)
    }

    fun handlePlayerAction(payload: String, session: WebSocketSession) {
        try {
            val action = ObjectMapper().readValue(payload, WebSocketAction::class.java)
            when (action.action) {
                WebSocketActionType.CONNECT_PLAYER -> {
                    ObjectMapper().readValue(payload, ContentPlayerJoinMessage::class.java).let {
                        webSocketPlayerService.addServerSocket(it.id, session)
                    }
                }
                WebSocketActionType.DISCONNECT_PLAYER -> {
                    ObjectMapper().readValue(payload, ContentPlayerLeaveMessage::class.java).let {
                        webSocketPlayerService.removeServerSocket(it.id, it.sessionId)
                    }
                }
                WebSocketActionType.GET_PLAYER_STATE -> {
                    ObjectMapper().readValue(payload, ContentPlayerGetStateMessage::class.java).let {

                        // session.sendMessage(TextMessage(ContentPlayerStateResponse(it.id, botService.get)))
                    }

                }
            }
        } catch (e: Exception) {}
    }
}