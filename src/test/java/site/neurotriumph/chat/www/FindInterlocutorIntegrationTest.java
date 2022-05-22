package site.neurotriumph.chat.www;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
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
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.util.EventQueue;

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
    EventQueue eventQueue = new EventQueue(1);

    new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        System.out.println(message);
        try {
          eventQueue.add(objectMapper.readValue(message, Event.class));
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

    Event event = eventQueue.poll();
    assertNotNull(event);
    assertEquals(EventType.NO_ONE_TO_TALK, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnInterlocutorFoundMessage() throws Exception {
    EventQueue eventQueue = new EventQueue(1);

    new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        try {
          eventQueue.add(objectMapper.readValue(message, InterlocutorFoundEvent.class));
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
  }
}
