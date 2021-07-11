package io.github.novemdecillion.chat.scenario

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.novemdecillion.chat.ChatMessage
import io.github.novemdecillion.chat.MessageNode
import io.github.novemdecillion.chat.MessageType
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

data class Conversation (
  val title: String,
  val message: String,
  val next: List<Conversation>? = null,
  val footer: String? = null,
  val questionnaire: Boolean = false
)

@ConfigurationProperties(prefix = "app.scenario")
@ConstructorBinding
data class ScenarioProperties(
  val file: Resource
)

@Service
class ScenarioService(scenarioProperties: ScenarioProperties) {
  companion object {
    const val DELIMITER = "\\"
    const val KEY_SCENARIO = "${DELIMITER}scenario"
    const val KEY_QUESTIONNAIRE = "${DELIMITER}questionnaire"
    const val KEY_TIMEOUT = "${DELIMITER}time out"
    const val KEY_NO_RESULT = "${DELIMITER}no result"
    const val KEY_UNKNOWN_ERROR = "${DELIMITER}unknown error"
  }

  val conversations: Map<String?, Pair<Conversation, ChatMessage>>

  init {
    ObjectMapper(YAMLFactory())
      .registerKotlinModule()
      .readValue<List<Conversation>>(scenarioProperties.file.inputStream)
      .let { rootConversations ->
        val asciiDoctor: Asciidoctor = Asciidoctor.Factory.create()
        this.conversations = convertConversationToChatMessage(rootConversations, asciiDoctor, mutableMapOf())
      }
  }

  fun convertConversationToChatMessage(conversations: List<Conversation>, asciiDoctor: Asciidoctor, conversationMap: MutableMap<String?, Pair<Conversation, ChatMessage>>, path: String? = null): MutableMap<String?, Pair<Conversation, ChatMessage>> {
    conversations
      .forEach { conversation ->
        val key = "${path.orEmpty()}$DELIMITER${conversation.title}"

        check(!conversationMap.containsKey(key)) { "シナリオのタイトル「$key」が重複しています。" }

        val messageNodes = mutableListOf(MessageNode(asciiDoctor.convert(conversation.message, Options.builder().build())))
        conversation.next
          ?.forEach { nextConversation ->
            messageNodes.add(MessageNode(nextConversation.title, "${key.orEmpty()}$DELIMITER${nextConversation.title}"))
          }
        conversation.footer
          ?.also {
            messageNodes.add(MessageNode(asciiDoctor.convert(it, Options.builder().build())))
          }
        conversationMap[key] = conversation to ChatMessage(MessageType.SCENARIO, messageNodes, false)

        if (!conversation.next.isNullOrEmpty()) {
          convertConversationToChatMessage(conversation.next, asciiDoctor, conversationMap, key)
        }
      }

    return conversationMap
  }

  fun findChatMessage(title: String? = null): ChatMessage {
    return this.conversations[title]?.second ?: throw IllegalArgumentException()
  }

  fun findConversation(title: String? = null): Pair<Conversation, ChatMessage> {
    return this.conversations[title] ?: throw IllegalArgumentException()
  }
}