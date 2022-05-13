package site.neurotriumph.chat.www.interlocutor;

import java.io.IOException;
import java.util.Objects;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import site.neurotriumph.chat.www.pojo.Event;

public class Human extends Interlocutor {
  private final WebSocketSession webSocketSession;

  public Human(WebSocketSession webSocketSession) {
    super(true);
    this.webSocketSession = webSocketSession;
  }

  @Override
  public void send(Event event) throws IOException {
    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    return Objects.equals(webSocketSession, ((Human) o).webSocketSession);
  }

  @Override
  public int hashCode() {
    return Objects.hash(webSocketSession);
  }
}
