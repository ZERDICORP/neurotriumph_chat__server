package site.neurotriumph.chat.www;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.util.EventQueue;
import site.neurotriumph.chat.www.util.WebSocketClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class SendMessageToHumanIntegrationTest {
  private String baseUrl;
  @Value("${app.lobby_spent_time}")
  private long lobbySpentTime;
  @LocalServerPort
  private int serverPort;
  @Autowired
  private LobbyService lobbyService;
  @Autowired
  private ObjectMapper objectMapper;

  @Before
  public void before() {
    baseUrl = "ws://localhost:" + serverPort + "/api/v1/bind";

    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", 30000);
  }

  @After
  public void after() {
    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", lobbySpentTime);
  }

  @Test
  public void shouldReceiveChatMessages() throws Exception {
    final String messageToSend = "Hello, world!";

    // First user.
    final WebSocketClient firstWebSocketClient = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> {
            InterlocutorFoundEvent interlocutorFoundEvent = objectMapper.readValue(message,
              InterlocutorFoundEvent.class);

            if (interlocutorFoundEvent.isAbleToWrite()) {
              client.send(objectMapper.writeValueAsString(new ChatMessageEvent(messageToSend)));
            }
          }
          case CHAT_MESSAGE -> client.addEvent(objectMapper.readValue(message, ChatMessageEvent.class));
        }
      })
      .connectWithBlocking();

    // Second user.
    final WebSocketClient secondWebSocketClient = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> {
            InterlocutorFoundEvent interlocutorFoundEvent = objectMapper.readValue(message,
              InterlocutorFoundEvent.class);

            if (interlocutorFoundEvent.isAbleToWrite()) {
              client.send(objectMapper.writeValueAsString(new ChatMessageEvent(messageToSend)));
            }
          }
          case CHAT_MESSAGE -> client.addEvent(objectMapper.readValue(message, ChatMessageEvent.class));
        }
      })
      .connectWithBlocking();

    firstWebSocketClient.waitUntilEventQueueIsFull();
    secondWebSocketClient.waitUntilEventQueueIsFull();

    final EventQueue firstEventQueue = firstWebSocketClient.closeAndReturnEventQueue();
    final EventQueue secondEventQueue = secondWebSocketClient.closeAndReturnEventQueue();

    final ChatMessageEvent firstChatMessageEvent = (ChatMessageEvent) firstEventQueue.poll();
    assertNotNull(firstChatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, firstChatMessageEvent.getType());
    assertEquals(messageToSend, firstChatMessageEvent.getMessage());

    final ChatMessageEvent secondChatMessageEvent = (ChatMessageEvent) secondEventQueue.poll();
    assertNotNull(secondChatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, secondChatMessageEvent.getType());
    assertEquals(messageToSend, secondChatMessageEvent.getMessage());
  }
}
