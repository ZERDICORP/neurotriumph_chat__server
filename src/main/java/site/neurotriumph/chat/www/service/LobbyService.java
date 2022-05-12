package site.neurotriumph.chat.www.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import site.neurotriumph.chat.www.constant.Message;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LobbyService {
  @Value("${lobbySpentTime}")
  private Integer lobbySpentTime;

  private final Random random;
  private final ScheduledExecutorService executorService;
  private final List<WebSocketSession> lobby;

  {
    random = new Random();
    executorService = Executors.newSingleThreadScheduledExecutor();
    lobby = new ArrayList<>();
  }

  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;

  public Interlocutor findInterlocutor(WebSocketSession session) throws IOException {
    // Using the Random::nextInt() function with parameter 2 (which means
    // getting one of two numbers - 0 or 1), we choose with whom the user
    // will communicate, with a machine (neural network) or with a person
    // (another web socket session).
    final int choice = random.nextInt(2);

    // When choice == 0, we take a neural network as an interlocutor.
    if (choice == 0) {
      // Looking for one random neural network in the database.
      final Optional<NeuralNetwork> neuralNetwork = neuralNetworkRepository.findOneRandom();
      if (neuralNetwork.isPresent()) {
        return new Machine(neuralNetwork.get());
      }

      // If there is not a single neural network in the database, and
      // at the same time the lobby is empty, we must inform the user
      // about this and abort the work by returning null.
      if (lobby.size() == 0) {
        session.sendMessage(new TextMessage(Message.NO_ONE_TO_TALK));
        session.close();
        return null;
      }
    }

    // When choice == 1, we take another websocket session as the
    // interlocutor.

    // If there is at least one user in the list, then we take it and
    // return as a human.
    if (lobby.size() != 0) {
      return new Human(lobby.remove(0));
    }

    // If the list is empty, we add a user there in the expectation that
    // another user will come and choose us as an interlocutor.
    lobby.add(session);

    // After N seconds, we have to check whether someone invited us or not.
    executorService.schedule(() -> executeIfTooLongInLobby(session), lobbySpentTime, TimeUnit.MILLISECONDS);

    // By returning null, we oblige the caller to do nothing, but simply
    // complete their work.
    return null;
  }

  public void executeIfTooLongInLobby(WebSocketSession session) {
    // If our session is not in the list, then we have already been
    // selected as an interlocutor by another user, and you just need
    // to complete this function.
    if (!lobby.contains(session)) {
      return;
    }

    // Since we are waiting too long, we remove our session from the list.
    lobby.remove(session);

    // Now let's look for a random neural network as an interlocutor.
    final Optional<NeuralNetwork> neuralNetwork = neuralNetworkRepository.findOneRandom();
    try {
      // If there is not a single neural network in the database, we must
      // inform the user about this and terminate the function.
      if (neuralNetwork.isEmpty()) {
        session.sendMessage(new TextMessage(Message.NO_ONE_TO_TALK));
        session.close();
        return;
      }

      // TODO: create a new room right here

      session.sendMessage(new TextMessage(Message.INTERLOCUTOR_FOUND));
    } catch (IOException e) {
      System.out.println("Message sending error: " + e);
    }
  }
}
