package site.neurotriumph.chat.www.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LobbyStorage {
  private final Map<Interlocutor, ScheduledFuture<?>> map;

  {
    map = new HashMap<>();
  }

  public ScheduledFuture<?> exclude(Interlocutor user) {
    return map.remove(user);
  }

  public int size() {
    return map.size();
  }

  public Interlocutor getFirst() {
    return map.keySet().stream()
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("impossible situation, key is NULL"));
  }

  public void add(Interlocutor interlocutor, ScheduledFuture<?> scheduledFuture) {
    map.put(interlocutor, scheduledFuture);
  }
}
