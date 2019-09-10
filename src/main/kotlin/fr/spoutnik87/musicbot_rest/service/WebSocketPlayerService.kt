package fr.spoutnik87.musicbot_rest.service

import com.fasterxml.jackson.databind.ObjectMapper
import fr.spoutnik87.musicbot_rest.viewmodel.BotServerViewModel
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

/**
 * @param id Server's id
 */
data class ContentPlayerJoinMessage(val id: String)

/**
 * @param id Server's id
 * @param sessionId Unique id the session created during session creation.
 */
data class ContentPlayerLeaveMessage(val id: String, val sessionId: String)

/**
 * @param id Server's id
 */
data class ContentPlayerGetStateMessage(val id: String)

/**
 * @param id Server's id
 * @param state Server's state
 */
data class ContentPlayerStateResponse(val id: String, val state: BotServerViewModel)

@Service
class WebSocketPlayerService {

    private val serverSockets = LinkedHashMap<String, ArrayList<WebSocketSession>>()

    /**
     * @param id Server's UUID
     * @param session Web socket session
     */
    fun addServerSocket(id: String, session: WebSocketSession) {
        if (serverSockets[id] != null) {
            serverSockets[id]?.add(session)
        } else {
            val list = ArrayList<WebSocketSession>()
            list.add(session)
            serverSockets[id] = list
        }
    }

    /**
     * @param id Server's UUID
     * @param sessionId Web socket session's ID
     */
    fun removeServerSocket(id: String, sessionId: String) {
        if (serverSockets[id] != null) {
            serverSockets[id]?.removeIf { it.id == sessionId }
        }
    }

    /**
     * @param id Server's UUID
     */
    fun sendMessage(id: String, server: BotServerViewModel) {
        serverSockets.filter { it.key == id }.flatMap { it.value }.forEach {
            try {
                it.sendMessage(TextMessage(ObjectMapper().writeValueAsString(ContentPlayerStateResponse(id, server))))
            } catch (e: Exception) {
                removeServerSocket(id, it.id)
            }
        }
    }
}