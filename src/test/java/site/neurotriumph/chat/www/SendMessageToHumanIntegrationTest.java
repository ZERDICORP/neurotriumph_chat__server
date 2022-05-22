package site.neurotriumph.chat.www;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.util.EventQueue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class SendMessageToHumanIntegrationTest {
  private String baseUrl;
  @Value("${app.lobby_spent_time}")
  private long originalLobbySpentTime;
  @LocalServerPort
  private int serverPort;
  @Autowired
  private LobbyService lobbyService;
  @Autowired
  private ObjectMapper objectMapper;

  @Before
  public void before() {
    baseUrl = "ws://localhost:" + serverPort + "/api/v1/bind";

    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", 10000);
  }

  @After
  public void after() {
    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", originalLobbySpentTime);
  }

  @Test
  public void shouldReceiveChatMessages() throws Exception {
    EventQueue eventQueue = new EventQueue(2);

    String messageToSend = "Hello, world!";

    // First user.
    WebSocketClient firstWebSocketClient = new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        try {
          Event event = objectMapper.readValue(message, Event.class);
          switch (event.getType()) {
            case INTERLOCUTOR_FOUND -> {
              InterlocutorFoundEvent interlocutorFoundEvent = objectMapper.readValue(message,
                InterlocutorFoundEvent.class);

              if (interlocutorFoundEvent.isAbleToWrite()) {
                send(objectMapper.writeValueAsString(new ChatMessageEvent(messageToSend)));
              }
            }
            case CHAT_MESSAGE -> eventQueue.add(objectMapper.readValue(message, ChatMessageEvent.class));
          }
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
      }
    };
    firstWebSocketClient.connectBlocking();

    // Second user.
    WebSocketClient secondWebSocketClient = new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        try {
          Event event = objectMapper.readValue(message, Event.class);
          switch (event.getType()) {
            case INTERLOCUTOR_FOUND -> {
              InterlocutorFoundEvent interlocutorFoundEvent = objectMapper.readValue(message,
                InterlocutorFoundEvent.class);

              if (interlocutorFoundEvent.isAbleToWrite()) {
                send(objectMapper.writeValueAsString(new ChatMessageEvent(messageToSend)));
              }
            }
            case CHAT_MESSAGE -> eventQueue.add(objectMapper.readValue(message, ChatMessageEvent.class));
          }
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
      }
    };
    secondWebSocketClient.connectBlocking();

    eventQueue.waitUntilFull();

    ChatMessageEvent firstChatMessageEvent = (ChatMessageEvent) eventQueue.poll();
    assertNotNull(firstChatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, firstChatMessageEvent.getType());
    assertEquals(messageToSend, firstChatMessageEvent.getMessage());

    ChatMessageEvent secondChatMessageEvent = (ChatMessageEvent) eventQueue.poll();
    assertNotNull(secondChatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, secondChatMessageEvent.getType());
    assertEquals(messageToSend, secondChatMessageEvent.getMessage());

//    firstWebSocketClient.close();
//    secondWebSocketClient.close();
//
//    Thread.sleep(1000);
  }
}
