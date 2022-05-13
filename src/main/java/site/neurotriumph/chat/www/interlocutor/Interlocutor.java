package site.neurotriumph.chat.www.interlocutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import site.neurotriumph.chat.www.pojo.Event;

public abstract class Interlocutor {
  protected final ObjectMapper objectMapper;
  protected final boolean isHuman;

  {
    objectMapper = new ObjectMapper();
  }

  public Interlocutor(boolean isHuman) {
    this.isHuman = isHuman;
  }

  public boolean isHuman() {
    return isHuman;
  }

  public abstract void send(Event Event) throws IOException;
}
