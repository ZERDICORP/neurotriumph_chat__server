package site.neurotriumph.chat.www.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageEvent extends Event {
  private String message;

  {
    type = EventType.CHAT_MESSAGE;
  }

  public ChatMessageEvent(String message) {
    this.message = message;
  }
}
