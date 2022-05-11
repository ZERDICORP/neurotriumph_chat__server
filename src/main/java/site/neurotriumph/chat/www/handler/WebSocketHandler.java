package site.neurotriumph.chat.www.handler;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class WebSocketHandler extends TextWebSocketHandler {
  private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.info("Connected: " + session.getRemoteAddress());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    logger.info("Message: " + message + ", from: " + session.getRemoteAddress());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    logger.info("Disconnected: " + session.getRemoteAddress());
  }
}
