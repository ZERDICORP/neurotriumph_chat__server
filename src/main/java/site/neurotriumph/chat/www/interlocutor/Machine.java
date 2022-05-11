package site.neurotriumph.chat.www.interlocutor;

import org.springframework.web.socket.TextMessage;

public class Machine implements Interlocutor {
  @Override
  public void send(TextMessage message) {
    System.out.println("Message sent to neural network: " + message);
  }
}
