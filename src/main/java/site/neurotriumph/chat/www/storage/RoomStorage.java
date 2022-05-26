package site.neurotriumph.chat.www.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.room.Room;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RoomStorage {
  private final Map<Room, ScheduledFuture<?>> map;

  {
    map = new HashMap<>();
  }

  public ScheduledFuture<?> exclude(Room room) {
    return map.remove(room);
  }

  public Optional<Room> findByInterlocutor(Interlocutor interlocutor) {
    return map.keySet().stream()
      .filter(r -> r.has(interlocutor))
      .findFirst();
  }

  public Room createNew(Interlocutor firstInterlocutor, Interlocutor secondInterlocutor) {
    final Room room = new Room(firstInterlocutor, secondInterlocutor);
    map.put(room, null);
    return room;
  }

  public void addTask(Room room, ScheduledFuture<?> scheduledFuture) {
    map.put(room, scheduledFuture);
  }
}
