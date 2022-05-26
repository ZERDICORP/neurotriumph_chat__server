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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.util.EchoServer;
import site.neurotriumph.chat.www.util.EventQueue;
import site.neurotriumph.chat.www.util.WebSocketClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("/test.properties")
public class SendMessageToMachineIntegrationTest {
  private String baseUrl;
  private EchoServer echoServer;
  @LocalServerPort
  private int serverPort;
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
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReceiveChatMessage() throws Exception {
    final EventQueue eventQueue = new WebSocketClient(baseUrl, objectMapper, 1)
      .setOnMessage((message, eventType, client) -> {
        switch (eventType) {
          case INTERLOCUTOR_FOUND -> {
            InterlocutorFoundEvent interlocutorFoundEvent = objectMapper.readValue(message,
              InterlocutorFoundEvent.class);

            if (interlocutorFoundEvent.isAbleToWrite()) {
              client.send(objectMapper.writeValueAsString(new ChatMessageEvent("Hello, world!")));
            }
          }
          case CHAT_MESSAGE -> client.addEvent(objectMapper.readValue(message,
            ChatMessageEvent.class));
        }
      })
      .connectWithBlocking()
      .waitUntilEventQueueIsFull()
      .closeAndReturnEventQueue();

    final ChatMessageEvent chatMessageEvent = (ChatMessageEvent) eventQueue.poll();
    assertNotNull(chatMessageEvent);
    assertEquals(EventType.CHAT_MESSAGE, chatMessageEvent.getType());
    assertEquals("Hello, world!", chatMessageEvent.getMessage());
  }
}
