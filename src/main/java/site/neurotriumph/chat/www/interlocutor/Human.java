package site.neurotriumph.chat.www.interlocutor;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Human extends Interlocutor {
  private final WebSocketSession webSocketSession;

  public Human(WebSocketSession webSocketSession) {
    super(true);
    this.webSocketSession = webSocketSession;
  }

  @Override
  public void send(TextMessage message) throws IOException {
    webSocketSession.sendMessage(message);
  }
}
