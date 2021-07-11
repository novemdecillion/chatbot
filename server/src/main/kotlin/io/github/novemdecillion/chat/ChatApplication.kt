package io.github.novemdecillion.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableWebSocket
class ChatApplication(val chatHandler: ChatSocketHandler) : WebSocketConfigurer {
  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
    registry.addHandler(chatHandler, "/conversation").setAllowedOrigins(CorsConfiguration.ALL)
  }
}

fun main(args: Array<String>) {
    runApplication<ChatApplication>(*args)
}
