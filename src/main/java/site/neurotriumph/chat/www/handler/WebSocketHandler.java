package site.neurotriumph.chat.www.handler;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import site.neurotriumph.chat.www.constant.Message;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.service.LobbyService;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
  @Autowired
  private LobbyService lobbyService;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws IOException {
    Interlocutor interlocutor = lobbyService.findInterlocutor(session);
    // If we do find someone to talk to, we need to create a room and
    // let the interlocutors know that they can start a conversation.
    if (interlocutor != null) {

      // TODO: create a new room right here

      session.sendMessage(new TextMessage(Message.INTERLOCUTOR_FOUND)); // TODO: send a normal message (json object)
      // For the found interlocutor, obviously, we will report the possibility
      // of starting a dialogue only if the interlocutor is a human.
      if (interlocutor.isHuman()) {
        interlocutor.send(new TextMessage(Message.INTERLOCUTOR_FOUND)); // TODO: send a normal message (json object)
      }
    }
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
