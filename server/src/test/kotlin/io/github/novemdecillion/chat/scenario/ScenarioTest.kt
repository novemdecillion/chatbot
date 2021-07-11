package io.github.novemdecillion.chat.scenario

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ScenarioTest {
  @Test
  fun test() {
    ScenarioTest::class.java.getResourceAsStream("/sample-scenario.yml")
      .use { inputStream ->
        val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val conversations = objectMapper.readValue<List<Conversation>>(inputStream)
      }
  }
}