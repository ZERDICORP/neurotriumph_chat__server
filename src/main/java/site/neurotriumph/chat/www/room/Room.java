package site.neurotriumph.chat.www.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;

public class Room {
  @Getter
  private final List<Interlocutor> interlocutors;
  @Getter
  private Date timePoint;
  @Getter
  private int messageCounter;

  {
    interlocutors = new ArrayList<>();
    updateTimePoint();
  }

  public Room(Interlocutor firstInterlocutor, Interlocutor secondInterlocutor) {
    interlocutors.add(firstInterlocutor);
    interlocutors.add(secondInterlocutor);
  }

  public boolean has(Interlocutor interlocutor) {
    return interlocutors.contains(interlocutor);
  }

  public void increaseMessageCounter() {
    messageCounter++;
  }

  public void updateTimePoint() {
    timePoint = new Date();
  }

  public void swapInterlocutors() {
    Collections.rotate(interlocutors, 1);
  }

  public Interlocutor getFirstInterlocutor() {
    return interlocutors.get(0);
  }

  public Interlocutor getSecondInterlocutor() {
    return interlocutors.get(1);
  }
}
