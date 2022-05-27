package site.neurotriumph.chat.www.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;

public class Room {
  private final String id;
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
    id = ((Human) firstInterlocutor).getId();
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

  public Interlocutor getAnotherInterlocutor(Interlocutor interlocutor) {
    return getFirstInterlocutor().equals(interlocutor) ?
      getSecondInterlocutor() : getFirstInterlocutor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    return Objects.equals(id, ((Room) o).id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
