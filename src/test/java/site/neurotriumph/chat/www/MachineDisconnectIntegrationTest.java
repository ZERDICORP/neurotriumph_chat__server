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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class MachineDisconnectIntegrationTest {
  private String baseUrl;
  @LocalServerPort
  private int serverPort;
  @Autowired
  private ObjectMapper objectMapper;

  @Before
  public void setup() {
    baseUrl = "ws://localhost:" + serverPort + "/api/v1/bind";
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReceiveDisconnectEventAndAtTheNextConnectionReceiveNoOneToTalkEvent() throws Exception {
    BlockingQueue<Event> blockingQueue = new ArrayBlockingQueue<>(1);

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
                send(objectMapper.writeValueAsString(new ChatMessageEvent("Hello, world!")));
              }
            }
            case DISCONNECT -> blockingQueue.add(objectMapper.readValue(message,
              DisconnectEvent.class));
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

    DisconnectEvent disconnectEvent = (DisconnectEvent) blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(disconnectEvent);
    assertEquals(EventType.DISCONNECT, disconnectEvent.getType());
    assertEquals(DisconnectReason.INTERLOCUTOR_DISCONNECTED, disconnectEvent.getReason());

    // The next connection should receive a NO_ONE_TO_TALK event because
    // the neural network has become inactive since the previous connection.
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
                send(objectMapper.writeValueAsString(new ChatMessageEvent("Hello, world!")));
              }
            }
            case NO_ONE_TO_TALK -> blockingQueue.add(objectMapper.readValue(message,
              Event.class));
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

    Event event = (Event) blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(event);
    assertEquals(EventType.NO_ONE_TO_TALK, event.getType());
  }
}
