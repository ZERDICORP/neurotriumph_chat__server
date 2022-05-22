package site.neurotriumph.chat.www;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.pojo.Choice;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.pojo.MakeChoiceEvent;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.util.EchoServer;
import site.neurotriumph.chat.www.util.EventQueue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class MakeChoiceIntegrationTest {
  private String baseUrl;
  private EchoServer echoServer;
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

    echoServer = new EchoServer();

    new Thread(echoServer).start();
  }

  @After
  public void after() throws IOException {
    echoServer.stop();
  }

  @Test
  public void shouldReceiveDisconnectEvent() throws Exception {
    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", 10000);

    EventQueue eventQueue = new EventQueue(1);

    // First user (will make a choice).
    new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        try {
          Event event = objectMapper.readValue(message, Event.class);
          if (event.getType() == EventType.INTERLOCUTOR_FOUND) {
            send(objectMapper.writeValueAsString(new MakeChoiceEvent(Choice.ITS_A_HUMAN)));
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

    Thread.sleep(1000);

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

    eventQueue.waitUntilFull();

    DisconnectEvent disconnectEvent = (DisconnectEvent) eventQueue.poll();
    assertNotNull(disconnectEvent);
    assertEquals(EventType.DISCONNECT, disconnectEvent.getType());
    assertEquals(DisconnectReason.INTERLOCUTOR_MAKE_A_CHOICE, disconnectEvent.getReason());

    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", lobbySpentTime);
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendIDKChoiceAndReceiveItWasAMachineEvent() throws Exception {
    EventQueue eventQueue = new EventQueue(2);

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

              send(objectMapper.writeValueAsString(new MakeChoiceEvent(Choice.IDK)));

              eventQueue.add(interlocutorFoundEvent);
            }
            case IT_WAS_A_MACHINE -> eventQueue.add(objectMapper.readValue(message,
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

    eventQueue.waitUntilFull();

    InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) eventQueue.poll();
    assertNotNull(interlocutorFoundEvent);
    assertNotNull(interlocutorFoundEvent.getTimeLabel());
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());

    Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.IT_WAS_A_MACHINE, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendItsAHumanChoiceAndReceiveYouAreWrongEvent() throws Exception {
    EventQueue eventQueue = new EventQueue(2);

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

              send(objectMapper.writeValueAsString(new MakeChoiceEvent(Choice.ITS_A_HUMAN)));

              eventQueue.add(interlocutorFoundEvent);
            }
            case YOU_ARE_WRONG -> eventQueue.add(objectMapper.readValue(message,
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

    eventQueue.waitUntilFull();

    InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) eventQueue.poll();
    assertNotNull(interlocutorFoundEvent);
    assertNotNull(interlocutorFoundEvent.getTimeLabel());
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());

    Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.YOU_ARE_WRONG, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendItsAMachineChoiceAndReceiveYouAreRightEvent() throws Exception {
    EventQueue eventQueue = new EventQueue(2);

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

              send(objectMapper.writeValueAsString(new MakeChoiceEvent(Choice.ITS_A_MACHINE)));

              eventQueue.add(interlocutorFoundEvent);
            }
            case YOU_ARE_RIGHT -> eventQueue.add(objectMapper.readValue(message,
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

    eventQueue.waitUntilFull();

    InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) eventQueue.poll();
    assertNotNull(interlocutorFoundEvent);
    assertNotNull(interlocutorFoundEvent.getTimeLabel());
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());

    Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.YOU_ARE_RIGHT, event.getType());
  }
}
