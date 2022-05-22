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
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.util.EventQueue;

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

    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", 10000);
  }

  @After
  public void after() {
    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", originalLobbySpentTime);
  }

  @Test
  public void shouldReceiveDisconnectEvent() throws Exception {
    EventQueue eventQueue = new EventQueue(1);

    // First user (will be disconnected).
    WebSocketClient firstWebSocketClient = new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
      }
    };
    firstWebSocketClient.connectBlocking();

    // Second user (will receive a DISCONNECT event).
    new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        try {
          Event event = objectMapper.readValue(message, Event.class);
          if (event.getType() == EventType.DISCONNECT) {
            eventQueue.add(objectMapper.readValue(message, DisconnectEvent.class));
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

    // Disconnect the first user.
    firstWebSocketClient.close();

    eventQueue.waitUntilFull();

    DisconnectEvent disconnectEvent = (DisconnectEvent) eventQueue.poll();
    assertNotNull(disconnectEvent);
    assertEquals(EventType.DISCONNECT, disconnectEvent.getType());
    assertEquals(DisconnectReason.INTERLOCUTOR_DISCONNECTED, disconnectEvent.getReason());
  }
}
