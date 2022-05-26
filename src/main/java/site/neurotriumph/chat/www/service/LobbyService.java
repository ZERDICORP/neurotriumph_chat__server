package site.neurotriumph.chat.www.service;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;
import site.neurotriumph.chat.www.storage.LobbyStorage;

@Service
public class LobbyService {
  @Value("${app.lobby_spent_time}")
  private long lobbySpentTime;
  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;
  @Autowired
  private RoomService roomService;
  @Autowired
  public LobbyStorage lobbyStorage;
  @Autowired
  @Qualifier("random")
  private Random random;
  @Autowired
  private ScheduledExecutorService scheduledExecutorService;

  public boolean excludeFromLobby(Interlocutor user) {
    synchronized (lobbyStorage) {
      final ScheduledFuture<?> scheduledFuture = lobbyStorage.exclude(user);
      if (scheduledFuture != null) {
        scheduledFuture.cancel(false);
        return true;
      }
      return false;
    }
  }

  public Interlocutor findMachine() {
    final Optional<NeuralNetwork> foundNeuralNetwork = neuralNetworkRepository.findOneRandom();
    if (foundNeuralNetwork.isPresent()) {
      final NeuralNetwork neuralNetwork = foundNeuralNetwork.get();
      return new Machine(neuralNetwork)
        // If an error occurred while sending a message to the api of the
        // neural network, we must mark the neural network as invalid and
        // make it inactive so that it no longer participates in testing.
        .onError(() -> {
          neuralNetwork.setInvalid_api(true);
          neuralNetwork.setActive(false);
          neuralNetworkRepository.save(neuralNetwork);
        });
    }

    return null;
  }

  public Interlocutor findInterlocutor(Interlocutor joinedInterlocutor) {
    // Using the Random::nextInt() function with parameter 2 (which means
    // getting one of two numbers - 0 or 1), we choose who the user will
    // communicate with: a machine (neural network) or a person (another
    // web socket session).
    final int rand = random.nextInt(2);

    // When rand == 0, we take a neural network as an interlocutor.
    if (rand == 0) {
      final Interlocutor foundMachine = findMachine();
      if (foundMachine != null) {
        return foundMachine;
      }
    }

    // Else, when rand == 1  (well, or not a single neural network was
    // found), we take another websocket session as the interlocutor.

    // Why is there a synchronous block here? Imagine that the lobby list
    // contains 1 item. Without a synchronous block, the following situation
    // can happen:
    //
    // Thread #1: Checking if lobby.size() != 0 and getting a positive result;
    // Thread #2: Doing lobby.remove(0) and removing/getting the first element;
    // Thread #1: Doing lobby.remove(0) and getting null (which is not quite
    // right).
    synchronized (lobbyStorage) {
      // If there is at least one human in the lobby, then we return it.
      if (lobbyStorage.size() != 0) {
        final Interlocutor interlocutor = lobbyStorage.getFirst();

        if (!excludeFromLobby(interlocutor)) {
          throw new RuntimeException("interlocutor not removed");
        }

        return interlocutor;
      }

      // Else, if the lobby list is empty, we add a user there in the
      // expectation that another user will come and choose us as an
      // interlocutor.
      lobbyStorage.add(joinedInterlocutor,
        // After N seconds, we have to check whether someone invited us or not.
        scheduledExecutorService.schedule(() -> {
          try {
            afterSpentTimeInLobby(joinedInterlocutor);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }, lobbySpentTime, TimeUnit.MILLISECONDS));
    }

    // By returning null, we oblige the caller to do nothing, but simply
    // complete their work.
    return null;
  }

  public void afterSpentTimeInLobby(Interlocutor joinedInterlocutor) throws IOException {
    // If it was not possible to delete the user, then he is not in the
    // lobby, which means that another user invited him, and we should
    // just complete this method.
    if (!excludeFromLobby(joinedInterlocutor)) {
      return;
    }

    // Since no one invited us, we will have to look for an interlocutor
    // from the list of machines.
    final Interlocutor foundMachine = findMachine();
    if (foundMachine == null) {
      joinedInterlocutor.send(new Event(EventType.NO_ONE_TO_TALK));
      ((Human) joinedInterlocutor).close();
      return;
    }

    roomService.create(joinedInterlocutor, foundMachine);
  }
}
