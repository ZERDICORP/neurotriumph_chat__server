package site.neurotriumph.chat.www.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LobbyService {
  @Value("${app.lobby_spent_time}")
  private long lobbySpentTime;
  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;
  @Autowired
  private RoomService roomService;
  private final ScheduledExecutorService executorService;
  private final Random random;
  private final List<Interlocutor> lobby;
  private final Map<Interlocutor, ScheduledFuture<?>> scheduledTasks;

  {
    executorService = Executors.newSingleThreadScheduledExecutor();
    random = new Random();
    lobby = new ArrayList<>();
    scheduledTasks = new HashMap<>();
  }

  public void onUserDisconnect(Interlocutor user) {
    synchronized (lobby) {
      final boolean removed = lobby.remove(user);
      if (removed) {
        scheduledTasks.remove(user).cancel(false);
      }
    }
  }

  public Interlocutor findInterlocutor(Interlocutor joinedInterlocutor) {
    // Using the Random::nextInt() function with parameter 2 (which means
    // getting one of two numbers - 0 or 1), we choose with whom the user
    // will communicate, with a machine (neural network) or with a person
    // (another web socket session).
    final int rand = random.nextInt(2);

    // When rand == 0, we take a neural network as an interlocutor.
    if (rand == 0) {
      final Optional<NeuralNetwork> neuralNetwork = neuralNetworkRepository.findOneRandom();
      if (neuralNetwork.isPresent()) {
        return new Machine(neuralNetwork.get());
      }
    }

    // Else, when rand == 1, we take another websocket session as the
    // interlocutor.

    // Why is there a synchronous block here? Imagine that the lobby list
    // contains 1 item. Without a synchronous block, the following situation
    // can happen:
    //
    // Thread #1: Checking if lobby.size() == 0 and getting a positive result;
    // Thread #2: Doing lobby.remove(0) and removing/getting the first element;
    // Thread #1: Doing lobby.remove(0) and getting null (which is not quite
    // right).
    synchronized (lobby) {
      // If there is at least one human in the lobby, then we return it.
      if (lobby.size() != 0) {
        Interlocutor interlocutor = lobby.remove(0);

        // If the user is still in the lobby, then lobbySpentTime
        // has not yet passed, and we need to cancel the scheduled function
        // execution, as well as remove the entry from the HashMap.
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(interlocutor);
        if (scheduledTask != null) {
          scheduledTask.cancel(false);
        }

        return interlocutor;
      }
    }

    // Else, if the lobby list is empty, we add a user there in the
    // expectation that another user will come and choose us as an
    // interlocutor.
    lobby.add(joinedInterlocutor);

    // After N seconds, we have to check whether someone invited us or not.
    scheduledTasks.put(joinedInterlocutor, executorService.schedule(() -> {
      try {
        afterSpentTimeInLobby(joinedInterlocutor);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }, lobbySpentTime, TimeUnit.MILLISECONDS));

    // By returning null, we oblige the caller to do nothing, but simply
    // complete their work.
    return null;
  }

  public void afterSpentTimeInLobby(Interlocutor joinedInterlocutor) throws IOException {
    synchronized (lobby) {
      // Yes, it may happen that the scheduled task is running, but the user as
      // already been removed from the lobby. Take a look at the explanation
      // below:
      //
      // Thread #1: Doing lobby.remove(0) (in this::findInterlocutor(...) in synchronized block);
      // Thread #2: Invokes this method;
      // Thread #1: Doing schedules.remove(foundHuman).cancel(false).
      //
      // That is, a scheduled task can be executed after the user is removed
      // from the list, but even before it is canceled. So we need to make sure
      // the user is still in the lobby.
      if (!lobby.contains(joinedInterlocutor)) {
        return;
      }

      // Since we are waiting too long, we remove our session from the list.
      lobby.remove(joinedInterlocutor);

      // Removing a scheduled task.
      scheduledTasks.remove(joinedInterlocutor);
    }

    // Since no one invited us, we will have to look for an interlocutor
    // from the list of machines.
    final Optional<NeuralNetwork> neuralNetwork = neuralNetworkRepository.findOneRandom();
    // If there are no neural networks, then we need to inform the user
    // that there really is no one to talk to yet.
    if (neuralNetwork.isEmpty()) {
      joinedInterlocutor.send(new Event(EventType.NO_ONE_TO_TALK));
      ((Human) joinedInterlocutor).close();
      return;
    }

    roomService.create(joinedInterlocutor, new Machine(neuralNetwork.get()));
  }
}
