package site.neurotriumph.chat.www.interlocutor;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;

public abstract class Interlocutor {
  private final boolean isHuman;

  public Interlocutor(boolean isHuman) {
    this.isHuman = isHuman;
  }

  public boolean isHuman() {
    return isHuman;
  }

  public abstract void send(TextMessage message) throws IOException;
}
