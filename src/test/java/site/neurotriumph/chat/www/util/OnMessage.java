package site.neurotriumph.chat.www.util;

import site.neurotriumph.chat.www.pojo.EventType;

public interface OnMessage {
  void run(String message, EventType eventType, WebSocketClient client) throws Exception;
}
