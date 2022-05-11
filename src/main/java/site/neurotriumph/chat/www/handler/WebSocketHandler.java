package site.neurotriumph.chat.www.handler;

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
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    // handle connection
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    // handle messaging
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    // handle disconnection
  }
}
