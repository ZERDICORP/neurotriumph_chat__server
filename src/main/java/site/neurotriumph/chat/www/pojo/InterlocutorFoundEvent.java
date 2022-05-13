package site.neurotriumph.chat.www.pojo;

import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class InterlocutorFoundEvent extends Event {
  private Date timeLabel;
  private boolean ableToWrite;

  {
    type = EventType.INTERLOCUTOR_FOUND;
  }

  public InterlocutorFoundEvent(Date timeLabel, boolean ableToWrite) {
    this.timeLabel = timeLabel;
    this.ableToWrite = ableToWrite;
  }
}
