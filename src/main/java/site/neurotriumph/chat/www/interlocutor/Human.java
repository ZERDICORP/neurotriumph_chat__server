package site.neurotriumph.chat.www.interlocutor;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Human implements Interlocutor {
  private final WebSocketSession webSocketSession;

  public Human(WebSocketSession webSocketSession) {
    this.webSocketSession = webSocketSession;
  }

  @Override
  public void send(TextMessage message) throws IOException {
    webSocketSession.sendMessage(message);
  }
}
