package io.github.novemdecillion.chat.faq

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.novemdecillion.chat.ChatMessage
import io.github.novemdecillion.chat.MessageNode
import io.github.novemdecillion.chat.MessageType
import io.github.novemdecillion.chat.scenario.ScenarioService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.time.Duration

@ConditionalOnProperty(value = ["app.faq.okbiz.url"], havingValue = "")
@ConfigurationProperties(prefix = "app.faq.okbiz")
@ConstructorBinding
data class OKBIZFAQProperties(
  val url: String,
  val clientId: String,
  val clientSecret: String,
  val username: String,
  val password: String,
  val timeout: Long,
  val maxResult: Int,
  val basicAuthUsername: String? = null,
  val basicAuthPassword: String? = null
)

data class OAuthTokenResponse(
  val access_token: String,
  val token_type: Int,
  val expires_in: Int,
  val refresh_token: String,
  val created_at: Int
)

data class FaqAnswer(
  val pc: String?,
  val smartphone: String?,
  val mobile_common: String?,
  val docomo: String?,
  val au: String?,
  val softbank: String?
)

data class FaqSearchItem(
  val id: String,
  val wizard: Boolean,
  val title: String,
  val question: String,
  val answer: FaqAnswer,
  val faq_type: Int
)

data class FaqSearchResponse(
  val faq_list: List<FaqSearchItem> = emptyList(),
  val page: Int,
  val per_page: Int,
  val total_entries: Int
)

data class FaqCategory(
  val id: String,
  val name: String
)

data class FaqShowItem(
  val id: String,
  val wizard: Boolean,
  val category: List<FaqCategory>,
  val title: String,
  val question: String,
  val answer: FaqAnswer,
)

data class FaqShowResponse(
  val faq: FaqShowItem
)


@Service
@ConditionalOnProperty(value = ["app.faq.okbiz.url"], havingValue = "")
class OKBIZService(private val okbizFaqProperties: OKBIZFAQProperties, val scenarioService: ScenarioService,
                   restTemplateBuilder: RestTemplateBuilder, objectMapper: ObjectMapper) {
  companion object {
    val log = LoggerFactory.getLogger(OKBIZService::class.java)
  }

  private val restTemplate: RestTemplate = restTemplateBuilder
    .messageConverters(MappingJackson2HttpMessageConverter(objectMapper), FormHttpMessageConverter())
    .setConnectTimeout(Duration.ofSeconds(okbizFaqProperties.timeout))
    .setReadTimeout(Duration.ofSeconds(okbizFaqProperties.timeout))
    .build()

  private fun setBasicAuthIfNeed(headers: HttpHeaders) {
    if ((okbizFaqProperties.basicAuthUsername != null)
        && (okbizFaqProperties.basicAuthPassword != null)) {
      headers.setBasicAuth(okbizFaqProperties.basicAuthUsername, okbizFaqProperties.basicAuthPassword)
    }
  }

  fun token(): String? {
    val responseEntity = try {
      val header = HttpHeaders()
        .also {
          it.contentType = MediaType.APPLICATION_FORM_URLENCODED
          setBasicAuthIfNeed(it)
        }

      val queryParam = LinkedMultiValueMap<String, String>().also {
        it["operator"] = "true"
        it["client_id"] = okbizFaqProperties.clientId
        it["client_secret"] = okbizFaqProperties.clientSecret
        it["\$username"] = okbizFaqProperties.username
        it["\$password"] = okbizFaqProperties.password
      }
      val requestEntity = HttpEntity(queryParam, header)
      restTemplate.exchange<OAuthTokenResponse>(
        "${okbizFaqProperties.url}/oauth/token?grant_type=password",
        HttpMethod.POST,
        requestEntity)
    } catch (ex: Throwable) {
      log.error("トークンの取得に失敗しました。", ex)
      null
    }
    return responseEntity?.body?.access_token
  }

  fun search(searchWord: String, token: String? = null): Pair<ChatMessage, Boolean> {
    val resolvedToken = token
      ?: token()
      ?: run {
        return scenarioService.findChatMessage(ScenarioService.KEY_UNKNOWN_ERROR) to false
      }

    val responseEntity = try {
      val header = HttpHeaders()
        .also {
          setBasicAuthIfNeed(it)
        }
      val requestEntity = HttpEntity<Unit>(header)

      restTemplate.exchange<FaqSearchResponse>(
      "${okbizFaqProperties.url}/api/faq/search?access_token=${resolvedToken}&site_id=1&search_string=${searchWord}&search_type=1&per_page=${okbizFaqProperties.maxResult}",
        HttpMethod.GET,
        requestEntity)
    } catch (ex: Throwable) {
      log.error("FAQ検索に失敗しました。", ex)
      return scenarioService.findChatMessage(ScenarioService.KEY_UNKNOWN_ERROR) to false
    }

    val faqList = responseEntity.body?.faq_list
      ?: return scenarioService.findChatMessage(ScenarioService.KEY_NO_RESULT) to false

    return when(faqList.size) {
      0 -> {
        scenarioService.findChatMessage(ScenarioService.KEY_NO_RESULT) to false
      }
      1 -> {
        show(faqList.first().id, resolvedToken)
      }
      else -> {
        faqList
          .map {
            MessageNode(it.title, it.id)
          }
          .let {
            ChatMessage(MessageType.TALK, it, false) to true
          }
      }
    }
  }

  fun show(faqId: String, token: String? = null): Pair<ChatMessage, Boolean> {
    val resolvedToken = token
      ?: token()
      ?: run {
        return scenarioService.findChatMessage(ScenarioService.KEY_UNKNOWN_ERROR) to false
      }

    return try {
      val header = HttpHeaders()
        .also {
          setBasicAuthIfNeed(it)
        }
      val requestEntity = HttpEntity<Unit>(header)

      val responseEntity = restTemplate.exchange<FaqShowResponse>(
        "${okbizFaqProperties.url}/api/faq/show?access_token=${resolvedToken}&site_id=1&id=${faqId}",
          HttpMethod.GET,
          requestEntity
        )

      responseEntity.body?.faq?.answer?.pc
        ?.let {
          ChatMessage(MessageType.TALK, it, false) to true
        }
        ?: (scenarioService.findChatMessage(ScenarioService.KEY_UNKNOWN_ERROR) to false)

    } catch (ex: Throwable) {
      log.error("FAQ取得に失敗しました。", ex)
      scenarioService.findChatMessage(ScenarioService.KEY_UNKNOWN_ERROR) to false
    }
  }
}