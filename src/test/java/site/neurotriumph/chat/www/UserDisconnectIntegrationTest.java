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
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.util.EventQueue;
import site.neurotriumph.chat.www.util.WebSocketClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class UserDisconnectIntegrationTest {
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

    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", 100000);
  }

  @After
  public void after() {
    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", originalLobbySpentTime);
  }

  @Test
  public void shouldReceiveDisconnectEvent() throws Exception {
    // First user (will be disconnected).
    final WebSocketClient firstWebSocketClient = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        if (eventType == EventType.INTERLOCUTOR_FOUND) {
          client.addEvent(objectMapper.readValue(message, InterlocutorFoundEvent.class));
        }
      })
      .connectWithBlocking();

    // Second user (will receive a DISCONNECT event).
    final WebSocketClient secondWebSocketClient = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        if (eventType == EventType.DISCONNECT) {
          client.addEvent(objectMapper.readValue(message, DisconnectEvent.class));
        }
      })
      .connectWithBlocking();

    firstWebSocketClient
      .waitUntilEventQueueIsFull()
      .close();

    final EventQueue eventQueue = secondWebSocketClient
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final DisconnectEvent disconnectEvent = (DisconnectEvent) eventQueue.poll();
    assertNotNull(disconnectEvent);
    assertEquals(EventType.DISCONNECT, disconnectEvent.getType());
    assertEquals(DisconnectReason.INTERLOCUTOR_DISCONNECTED, disconnectEvent.getReason());
  }
}
