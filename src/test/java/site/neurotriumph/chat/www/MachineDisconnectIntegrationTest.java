package site.neurotriumph.chat.www;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import site.neurotriumph.chat.www.util.EventQueue;
import site.neurotriumph.chat.www.util.WebSocketClient;

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
    final EventQueue firstEventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> {
            InterlocutorFoundEvent interlocutorFoundEvent = objectMapper.readValue(message,
              InterlocutorFoundEvent.class);

            if (interlocutorFoundEvent.isAbleToWrite()) {
              client.send(objectMapper.writeValueAsString(new ChatMessageEvent("Hello, world!")));
            }
          }
          case DISCONNECT -> client.addEvent(objectMapper.readValue(message, DisconnectEvent.class));
        }
      })
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final DisconnectEvent disconnectEvent = (DisconnectEvent) firstEventQueue.poll();
    assertNotNull(disconnectEvent);
    assertEquals(EventType.DISCONNECT, disconnectEvent.getType());
    assertEquals(DisconnectReason.INTERLOCUTOR_DISCONNECTED, disconnectEvent.getReason());

    // The next connection should receive a NO_ONE_TO_TALK event because
    // the neural network has become inactive since the previous connection.
    final EventQueue secondEventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> client.addEvent(
        objectMapper.readValue(message, Event.class)))
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final Event event = secondEventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.NO_ONE_TO_TALK, event.getType());
  }
}
