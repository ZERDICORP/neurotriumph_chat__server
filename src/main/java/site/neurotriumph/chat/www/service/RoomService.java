package site.neurotriumph.chat.www.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.Choice;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.pojo.MakeChoiceEvent;
import site.neurotriumph.chat.www.room.Room;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RoomService {
  @Value("${app.chat_messaging_delay}")
  private long chatMessagingDelay;
  @Value("${app.required_number_of_messages_to_make_a_choice}")
  private long requiredNumberOfMessagesToMakeChoice;
  private final ScheduledExecutorService executorService;
  private final Random random;
  private final List<Room> rooms;
  private final Map<Interlocutor, ScheduledFuture<?>> scheduledTasks;

  {
    executorService = Executors.newSingleThreadScheduledExecutor();
    random = new Random();
    rooms = new ArrayList<>();
    scheduledTasks = new HashMap<>();
  }

  public void removeAndCancelScheduledTask(Interlocutor interlocutor) {
    ScheduledFuture<?> scheduledTask = scheduledTasks.remove(interlocutor);
    if (scheduledTask != null) {
      scheduledTask.cancel(false);
    }
  }

  public Optional<Room> findRoom(Interlocutor sender) {
    return rooms.stream()
      .filter(r -> r.has(sender))
      .findFirst();
  }

  public void onUserDisconnect(Interlocutor user) throws IOException {
    final Optional<Room> foundRoom = findRoom(user);
    if (foundRoom.isEmpty()) {
      return;
    }

    final Room room = foundRoom.get();
    final Interlocutor interlocutor = room.getAnotherInterlocutor(user);

    if (interlocutor.isHuman()) {
      interlocutor.send(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED));
    }

    rooms.remove(room);
    // If the interlocutor is a machine, then the possible scheduled task
    // should be removed and canceled.
    removeAndCancelScheduledTask(user);
  }

  public void makeChoice(Interlocutor sender, MakeChoiceEvent makeChoiceEvent) throws IOException {
    final Optional<Room> foundRoom = findRoom(sender);
    if (foundRoom.isEmpty()) {
      return;
    }

    final Room room = foundRoom.get();
    // You can make a choice only after the N-th number of messages.
    if (room.getMessageCounter() < requiredNumberOfMessagesToMakeChoice) {
      return;
    }

    final Interlocutor interlocutor = room.getAnotherInterlocutor(sender);
    // If the interlocutor is a person, we will inform him that he is
    // disconnected from the chat, since his interlocutor has make a
    // choice.
    if (interlocutor.isHuman()) {
      interlocutor.send(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_MAKE_A_CHOICE));
    }

    rooms.remove(room);
    // If the interlocutor is a machine, then the possible scheduled task
    // should be removed and canceled.
    removeAndCancelScheduledTask(sender);

    // The user finds it difficult to choose.
    if (makeChoiceEvent.getChoice() == Choice.IDK) {
      sender.send(new Event(interlocutor.isHuman() ?
        EventType.IT_WAS_A_HUMAN : EventType.IT_WAS_A_MACHINE));
      return;
    }

    if ((makeChoiceEvent.getChoice() == Choice.ITS_A_HUMAN && interlocutor.isHuman()) ||
      (makeChoiceEvent.getChoice() == Choice.ITS_A_MACHINE && !interlocutor.isHuman())) {
      sender.send(new Event(EventType.YOU_ARE_RIGHT));
      return;
    }

    sender.send(new Event(EventType.YOU_ARE_WRONG));
  }

  public void sendMessage(Interlocutor sender, ChatMessageEvent chatMessageEvent) throws IOException {
    final Optional<Room> foundRoom = findRoom(sender);
    if (foundRoom.isEmpty()) {
      return;
    }

    final Room room = foundRoom.get();
    final Interlocutor firstInterlocutor = room.getFirstInterlocutor();
    final Interlocutor secondInterlocutor = room.getSecondInterlocutor();

    // The system in which the interlocutors speak in turn is achieved quite
    // simply: if the sender is in the first place in the room, then it is his
    // turn to speak (a simple list is hidden behind the room, on which the
    // rotation operation is performed).
    if (!firstInterlocutor.equals(sender)) {
      return;
    }

    // If N seconds have not passed since the last update of the time point,
    // then this means that the message was sent too early (and in the Turing
    // test it is important to have a delay between messages so that the machine
    // cannot be recognized by instantaneous answers).
    if (new Date().getTime() - room.getTimePoint().getTime() < chatMessagingDelay) {
      return;
    }

    // Forwarding a message to both interlocutors.
    firstInterlocutor.send(chatMessageEvent);
    secondInterlocutor.send(chatMessageEvent);

    // We swap interlocutors in order to prohibit the sender from sending messages,
    // and at the same time allow the recipient to do so.
    room.swapInterlocutors();
    // It is necessary to increase the message counter to check if there are enough
    // messages in the chat so that the user can make a choice (a machine is talking
    // to him, or a human).
    room.increaseMessageCounter();

    if (secondInterlocutor.isHuman()) {
      // We update the time point so that the recipient cannot immediately send the
      // message.
      room.updateTimePoint();
    } else {
      // Since secondInterlocutor is a machine, it means that the message was sent
      // to the API, and we should have received a response, which we then send to
      // the user.
      sendMachineResponse(((Machine) secondInterlocutor).getResponse(), firstInterlocutor, room);
    }
  }

  public void create(Interlocutor firstInterlocutor, Interlocutor secondInterlocutor) throws IOException {
    rooms.add(new Room(firstInterlocutor, secondInterlocutor));
    final Room room = rooms.get(rooms.size() - 1);

    // If rand == 0, then firstInterlocutor starts writing first, otherwise
    // (rand == 1) - starts writing secondInterlocutor.
    final int rand = random.nextInt(2);

    // If the second interlocutor writes first, we need to swap interlocutors
    // in places.
    if (rand == 1) {
      room.swapInterlocutors();
    }

    // Notifying the user that he has found an interlocutor.
    firstInterlocutor.send(new InterlocutorFoundEvent(room.getTimePoint(), rand == 0));

    // If the second interlocutor is a human, we also notify him.
    if (secondInterlocutor.isHuman()) {
      secondInterlocutor.send(new InterlocutorFoundEvent(room.getTimePoint(), rand == 1));
      return;
    }

    // If the second interlocutor is a machine, but the user writes first,
    // then we simply complete the function, otherwise we notify the machine
    // that it has to generate an initial message.
    if (rand != 1) {
      return;
    }

    // Sending a request to an API to create the initial chat message and get
    // a response.
    secondInterlocutor.send(new Event(EventType.INIT_CHAT_MESSAGE));

    // Forwarding machine response to the user.
    sendMachineResponse(((Machine) secondInterlocutor).getResponse(), firstInterlocutor, room);
  }

  public void sendMachineResponse(ChatMessageEvent response, Interlocutor user, Room room) throws IOException {
    // If the response of the neural network is null, then an error occurred
    // while sending, which means that the api is invalid, in which case we
    // can consider this as a disconnect.
    if (response == null) {
      user.send(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED));
      rooms.remove(room);
      return;
    }

    // We schedule to send the machine's response to the user (simulated delay).
    scheduledTasks.put(user, executorService.schedule(() -> {
      try {
        user.send(response);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      // We carry out all the same operations on the room.
      room.updateTimePoint();
      room.swapInterlocutors();
      room.increaseMessageCounter();
    }, chatMessagingDelay, TimeUnit.MILLISECONDS));
  }
}
