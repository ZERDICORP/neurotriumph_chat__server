package site.neurotriumph.chat.www.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DisconnectEvent extends Event {
  private DisconnectReason reason;

  {
    type = EventType.DISCONNECT;
  }

  public DisconnectEvent(DisconnectReason reason) {
    this.reason = reason;
  }
}
