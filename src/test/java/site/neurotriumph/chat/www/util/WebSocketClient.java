package site.neurotriumph.chat.www.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.handshake.ServerHandshake;
import site.neurotriumph.chat.www.pojo.Event;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
  private final ObjectMapper objectMapper;
  private final EventQueue eventQueue;
  private OnMessage onMessage;

  public WebSocketClient(String uri, ObjectMapper objectMapper, int eventQueueCapacity) throws URISyntaxException {
    super(new URI(uri));
    this.objectMapper = objectMapper;
    this.eventQueue = new EventQueue(eventQueueCapacity);
  }

  @Override
  public void onMessage(String message) {
    if (onMessage != null) {
      try {
        Event event = objectMapper.readValue(message, Event.class);
        onMessage.run(message, event.getType(), this);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public WebSocketClient setOnMessage(OnMessage onMessage) {
    this.onMessage = onMessage;
    return this;
  }

  public WebSocketClient connectWithBlocking() {
    try {
      connectBlocking();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public WebSocketClient waitUntilEventQueueIsFull() throws InterruptedException {
    eventQueue.waitUntilFull();
    return this;
  }

  @Override
  public void send(String message) {
    super.send(message);
  }

  public void addEvent(Event event) {
    if (!eventQueue.isFull()) {
      eventQueue.add(event);
    }
  }

  public EventQueue closeAndReturnEventQueue() throws InterruptedException {
    closeBlocking();
    return eventQueue;
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
  }

  @Override
  public void onClose(int i, String s, boolean b) {
  }

  @Override
  public void onError(Exception e) {
  }
}
