package site.neurotriumph.chat.www.handler;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.service.RoomService;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
  @Autowired
  private LobbyService lobbyService;
  @Autowired
  private RoomService roomService;

  @Override
  public void afterConnectionEstablished(WebSocketSession webSocketSession) throws IOException {
    Interlocutor connectedInterlocutor = new Human(webSocketSession);
    Interlocutor foundInterlocutor = lobbyService.findInterlocutor(connectedInterlocutor);
    // If we do find someone to talk to, we need to create a room and
    // let the interlocutors know that they can start a conversation.
    if (foundInterlocutor != null) {
      Room room = roomService.create(connectedInterlocutor, foundInterlocutor);

      // Inform the user that he can start sending a message.
      connectedInterlocutor.send(new InterlocutorFoundEvent(room.getTimePoint(), true));
      // For the found interlocutor, obviously, we will report the possibility
      // of starting a dialogue only if the interlocutor is a human.
      if (foundInterlocutor.isHuman()) {
        foundInterlocutor.send(new InterlocutorFoundEvent(room.getTimePoint(), false));
      }
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws IOException {
    // handle messaging
  }

  @Override
  public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus status) {
    lobbyService.exclude(new Human(webSocketSession));
  }
}
