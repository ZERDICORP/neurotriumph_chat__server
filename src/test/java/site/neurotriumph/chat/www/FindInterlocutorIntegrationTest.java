package site.neurotriumph.chat.www;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.chat.www.constant.Message;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class FindInterlocutorIntegrationTest {
  @LocalServerPort
  private int serverPort;
  private String baseUrl;

  @Before
  public void setup() {
    baseUrl = "ws://localhost:" + serverPort + "/api/v1/bind";
  }

  @Test
  public void shouldReturnNoOneToTalkMessage() throws Exception {
    BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

    new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        blockingQueue.add(message);
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
        ex.printStackTrace();
      }
    }.connectBlocking();

    // TODO: deserialize the message

    assertEquals(Message.NO_ONE_TO_TALK, blockingQueue.poll(2, TimeUnit.SECONDS));
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnInterlocutorFoundMessage() throws Exception {
    BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

    new WebSocketClient(new URI(baseUrl)) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
      }

      @Override
      public void onMessage(String message) {
        blockingQueue.add(message);
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
      }

      @Override
      public void onError(Exception ex) {
        ex.printStackTrace();
      }
    }.connectBlocking();

    // TODO: deserialize the message

    assertEquals(Message.INTERLOCUTOR_FOUND, blockingQueue.poll(2, TimeUnit.SECONDS));
  }
}
