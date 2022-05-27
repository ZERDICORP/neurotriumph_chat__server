package site.neurotriumph.chat.www.pojo;

import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class InterlocutorFoundEvent extends Event {
  private boolean ableToWrite;

  {
    type = EventType.INTERLOCUTOR_FOUND;
  }

  public InterlocutorFoundEvent(boolean ableToWrite) {
    this.ableToWrite = ableToWrite;
  }
}
