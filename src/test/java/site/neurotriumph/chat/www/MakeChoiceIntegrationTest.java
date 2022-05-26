package site.neurotriumph.chat.www;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
import site.neurotriumph.chat.www.util.WebSocketClient;

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
    new Thread(echoServer = new EchoServer()).start();
  }

  @After
  public void after() throws IOException {
    echoServer.stop();
  }

  @Test
  public void shouldReceiveDisconnectEvent() throws Exception {
    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", 30000);

    // First user (will make a choice).
    final WebSocketClient firstWebSocketClient = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        if (eventType == EventType.INTERLOCUTOR_FOUND) {
          client.addEvent(objectMapper.readValue(message, InterlocutorFoundEvent.class));
          client.send(objectMapper.writeValueAsString(new MakeChoiceEvent(Choice.ITS_A_HUMAN)));
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
    assertEquals(DisconnectReason.INTERLOCUTOR_MAKE_A_CHOICE, disconnectEvent.getReason());

    ReflectionTestUtils.setField(lobbyService, "lobbySpentTime", lobbySpentTime);
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendIDKChoiceAndReceiveItWasAMachineEvent() throws Exception {
    final EventQueue eventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> client.send(objectMapper.writeValueAsString(
            new MakeChoiceEvent(Choice.IDK)));
          case IT_WAS_A_MACHINE -> client.addEvent(objectMapper.readValue(message, Event.class));
        }
      })
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.IT_WAS_A_MACHINE, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendItsAHumanChoiceAndReceiveYouAreWrongEvent() throws Exception {
    final EventQueue eventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> client.send(objectMapper.writeValueAsString(
            new MakeChoiceEvent(Choice.ITS_A_HUMAN)));
          case YOU_ARE_WRONG -> client.addEvent(objectMapper.readValue(message, Event.class));
        }
      })
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.YOU_ARE_WRONG, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendItsAMachineChoiceAndReceiveYouAreRightEvent() throws Exception {
    final EventQueue eventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> client.send(objectMapper.writeValueAsString(
            new MakeChoiceEvent(Choice.ITS_A_MACHINE)));
          case YOU_ARE_RIGHT -> client.addEvent(objectMapper.readValue(message, Event.class));
        }
      })
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.YOU_ARE_RIGHT, event.getType());
  }
}
