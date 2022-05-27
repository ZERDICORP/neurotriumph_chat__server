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
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.util.EventQueue;
import site.neurotriumph.chat.www.util.WebSocketClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class FindInterlocutorIntegrationTest {
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
  public void shouldReturnNoOneToTalkMessage() throws Exception {
    final EventQueue eventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> client.addEvent(
        objectMapper.readValue(message, Event.class)))
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.NO_ONE_TO_TALK, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnInterlocutorFoundMessage() throws Exception {
    final EventQueue eventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> client.addEvent(
        objectMapper.readValue(message, InterlocutorFoundEvent.class)))
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) eventQueue.poll();
    assertNotNull(interlocutorFoundEvent);
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());
  }
}
