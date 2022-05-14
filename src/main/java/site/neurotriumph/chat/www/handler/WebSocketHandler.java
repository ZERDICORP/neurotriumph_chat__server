package site.neurotriumph.chat.www.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.service.RoomService;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
  @Autowired
  private LobbyService lobbyService;
  @Autowired
  private RoomService roomService;
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void afterConnectionEstablished(WebSocketSession webSocketSession) throws IOException {
    Interlocutor joinedInterlocutor = new Human(webSocketSession);
    Interlocutor foundInterlocutor = lobbyService.findInterlocutor(joinedInterlocutor);
    // If we do find someone to talk to, we need to create a room and
    // let the interlocutors know that they can start a conversation.
    if (foundInterlocutor != null) {
      roomService.create(joinedInterlocutor, foundInterlocutor);
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws IOException {
    Event event = objectMapper.readValue(message.getPayload(), Event.class);

    switch (event.getType()) {
      case CHAT_MESSAGE -> roomService.sendMessage(new Human(webSocketSession),
        objectMapper.readValue(message.getPayload(), ChatMessageEvent.class));
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus status) {
    final Interlocutor interlocutor = new Human(webSocketSession);

    lobbyService.exclude(interlocutor);
    roomService.exclude(interlocutor);
  }
}
