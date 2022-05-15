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
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.service.LobbyService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class DisconnectIntegrationTest {
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
  public void shouldReceiveDisconnectEvent() throws Exception {
    BlockingQueue<Event> blockingQueue = new ArrayBlockingQueue<>(1);

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
            blockingQueue.add(objectMapper.readValue(message, DisconnectEvent.class));
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

    DisconnectEvent disconnectEvent = (DisconnectEvent) blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(disconnectEvent);
    assertEquals(EventType.DISCONNECT, disconnectEvent.getType());
    assertEquals(DisconnectReason.INTERLOCUTOR_DISCONNECTED, disconnectEvent.getReason());
  }
}
