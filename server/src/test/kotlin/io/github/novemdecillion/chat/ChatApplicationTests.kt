package io.github.novemdecillion.chat

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.novemdecillion.chat.faq.OKBIZService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ChatApplicationTests(val okbizService: OKBIZService) {

  @Test
  fun contextLoads() {
    val (chatMessage, _) = okbizService.search("休暇")
    chatMessage.nodes
      .firstOrNull { !it.link.isNullOrEmpty() }
      ?.also {
        println(okbizService.show(it.link!!))
      }
  }

  fun deserializeZoneOffsetWithoutColon() {
    // TODO jackson-datatype-jsr310:2.13.0のリリース待ち
    // https://github.com/FasterXML/jackson-modules-java8/issues/38

    val mapper = ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))
      .registerModule(JavaTimeModule());

    val date: OffsetDateTime = mapper.readValue(""""2020-10-29T20:03:00+0900"""")
  }
}
