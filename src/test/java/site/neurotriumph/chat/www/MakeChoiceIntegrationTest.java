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
import site.neurotriumph.chat.www.pojo.Choice;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.pojo.MakeChoiceEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class MakeChoiceIntegrationTest {
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
  public void shouldSendIDKChoiceAndReceiveItWasAMachineEvent() throws Exception {
    BlockingQueue<Event> blockingQueue = new ArrayBlockingQueue<>(2);

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

              blockingQueue.add(interlocutorFoundEvent);
            }
            case IT_WAS_A_MACHINE -> blockingQueue.add(objectMapper.readValue(message,
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

    InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) blockingQueue.poll(2,
      TimeUnit.SECONDS);
    assertNotNull(interlocutorFoundEvent);
    assertNotNull(interlocutorFoundEvent.getTimeLabel());
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());

    Event event = blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(event);
    assertEquals(EventType.IT_WAS_A_MACHINE, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendItsAHumanChoiceAndReceiveYouAreWrongEvent() throws Exception {
    BlockingQueue<Event> blockingQueue = new ArrayBlockingQueue<>(2);

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

              blockingQueue.add(interlocutorFoundEvent);
            }
            case YOU_ARE_WRONG -> blockingQueue.add(objectMapper.readValue(message,
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

    InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) blockingQueue.poll(2,
      TimeUnit.SECONDS);
    assertNotNull(interlocutorFoundEvent);
    assertNotNull(interlocutorFoundEvent.getTimeLabel());
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());

    Event event = blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(event);
    assertEquals(EventType.YOU_ARE_WRONG, event.getType());
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldSendItsAMachineChoiceAndReceiveYouAreRightEvent() throws Exception {
    BlockingQueue<Event> blockingQueue = new ArrayBlockingQueue<>(2);

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

              blockingQueue.add(interlocutorFoundEvent);
            }
            case YOU_ARE_RIGHT -> blockingQueue.add(objectMapper.readValue(message,
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

    InterlocutorFoundEvent interlocutorFoundEvent = (InterlocutorFoundEvent) blockingQueue.poll(2,
      TimeUnit.SECONDS);
    assertNotNull(interlocutorFoundEvent);
    assertNotNull(interlocutorFoundEvent.getTimeLabel());
    assertEquals(EventType.INTERLOCUTOR_FOUND, interlocutorFoundEvent.getType());

    Event event = blockingQueue.poll(2, TimeUnit.SECONDS);
    assertNotNull(event);
    assertEquals(EventType.YOU_ARE_RIGHT, event.getType());
  }
}
