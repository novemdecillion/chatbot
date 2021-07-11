package io.github.novemdecillion.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import graphql.kickstart.tools.GraphQLQueryResolver
import graphql.kickstart.tools.GraphQLSubscriptionResolver
import graphql.schema.DataFetchingEnvironment
import io.github.novemdecillion.chat.faq.OKBIZService
import io.github.novemdecillion.chat.scenario.ScenarioService
import org.apache.commons.lang3.StringUtils
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

enum class MessageType {
  SCENARIO, TALK
}

data class SendMessage(
  val type: MessageType,
  val text: String,
  val link: String? = null
)

data class MessageNode(
  val text: String,
  val link: String? = null
)

interface IChatMessage {
  val type: MessageType?
  val nodes: List<MessageNode>
  val isSend: Boolean
}

data class ChatMessage (
  override val type: MessageType,
  override val nodes: List<MessageNode>,
  override val isSend: Boolean
): IChatMessage {
  constructor(type: MessageType, message: String, isSend: Boolean) : this(type, listOf(MessageNode(message)), isSend)

  override fun toString(): String {
    return nodes.joinToString(StringUtils.LF) { it.text }
  }
}

class SessionChatMessage(val sessionId: String, chatMessage: ChatMessage): IChatMessage by chatMessage

data class Session(val sessionId: String)

@Component
class ChatSocketHandler(val mapper: ObjectMapper, val scenarioService: ScenarioService, val okbizService: OKBIZService?, val logRepository: LogRepository)
  : TextWebSocketHandler(), GraphQLQueryResolver, GraphQLSubscriptionResolver {
  val sessions = ConcurrentHashMap<String, WebSocketSession>()
  val messageSink = Sinks.many().unicast().onBackpressureBuffer<SessionChatMessage>()

  override fun afterConnectionEstablished(session: WebSocketSession) {
    sessions.putIfAbsent(session.id, session)

    val replyMessage = scenarioService.findChatMessage(ScenarioService.KEY_SCENARIO)
    replayMessageWithLog(session, replyMessage, null, ScenarioService.KEY_SCENARIO)
  }

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    sessions.remove(session.id)
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    val sendMessage: SendMessage = mapper.readValue(message.payload)

    val chatMessage = ChatMessage(sendMessage.type, sendMessage.text, true)
    messageSink.tryEmitNext(SessionChatMessage(session.id, chatMessage))
    session.sendMessage(TextMessage(mapper.writeValueAsString(chatMessage)))

    when(sendMessage.type) {
      MessageType.SCENARIO -> {
        val (conversation, replyMessage) = scenarioService.findConversation(sendMessage.link)
        replayMessageWithLog(session, replyMessage, null, sendMessage.link)

        if (conversation.questionnaire) {
          val questionnaireMessage = scenarioService.findChatMessage(ScenarioService.KEY_QUESTIONNAIRE)
          replayMessageWithLog(session, questionnaireMessage, null, ScenarioService.KEY_QUESTIONNAIRE)
        }
      }
      MessageType.TALK -> okbizService?.also { service ->
        val (replyMessage, isFinish) = if (sendMessage.link.isNullOrEmpty()) {
          service.search(sendMessage.text)
        } else {
          service.show(sendMessage.link)
        }

        replayMessageWithLog(session, replyMessage, sendMessage.text)
        if (isFinish) {
          val questionnaireMessage = scenarioService.findChatMessage(ScenarioService.KEY_QUESTIONNAIRE)
          replayMessageWithLog(session, questionnaireMessage, null, ScenarioService.KEY_QUESTIONNAIRE)
        }
      }
    }
  }

  fun replayMessage(session: WebSocketSession, message: ChatMessage) {
    messageSink.tryEmitNext(SessionChatMessage(session.id, message))
    session.sendMessage(TextMessage(mapper.writeValueAsString(message)))
  }

  fun replayMessageWithLog(session: WebSocketSession, message: ChatMessage, input: String? = null, output: String? = null) {
    replayMessage(session, message)
    logRepository.insert(session.id, message.type.name, session.remoteAddress, input, output ?: message.toString())
  }

  fun sessions(): List<Session> {
    return sessions.map { (id, _) -> Session(id) }
  }

  fun message(environment: DataFetchingEnvironment): Publisher<SessionChatMessage> {
    return messageSink.asFlux()
  }
}