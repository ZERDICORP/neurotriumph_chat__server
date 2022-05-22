package site.neurotriumph.chat.www;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class SendMessageToHumanIntegrationTest {
  private String baseUrl;
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

  @Test
  public void shouldReceiveChatMessages() throws Exception {
    BlockingQueue<Event> blockingQueue = new ArrayBlockingQueue<>(1);

    final String messageToSend = "Hello, world!";

    // First user.
    new WebSocketClient(new URI(baseUrl)) {
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
            case CHAT_MESSAGE -> blockingQueue.add(objectMapper.readValue(message, ChatMessageEvent.class));
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
    }.connectBlocking();

    // Second user.
    new WebSocketClient(new URI(baseUrl)) {
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
            case CHAT_MESSAGE -> blockingQueue.add(objectMapper.readValue(message, ChatMessageEvent.class));
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
    }.connectBlocking();

    ChatMessageEvent firstChatMessageEvent = (ChatMessageEvent) blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(firstChatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, firstChatMessageEvent.getType());
    assertEquals(messageToSend, firstChatMessageEvent.getMessage());

    ChatMessageEvent secondChatMessageEvent = (ChatMessageEvent) blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(secondChatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, secondChatMessageEvent.getType());
    assertEquals(messageToSend, secondChatMessageEvent.getMessage());
  }
}
