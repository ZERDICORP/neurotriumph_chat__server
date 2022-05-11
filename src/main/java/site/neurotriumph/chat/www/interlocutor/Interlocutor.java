package site.neurotriumph.chat.www.interlocutor;

import java.io.IOException;
import org.springframework.web.socket.TextMessage;

public interface Interlocutor {
  void send(TextMessage message) throws IOException;
}
