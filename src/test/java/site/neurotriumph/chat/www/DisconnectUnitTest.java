package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.util.SpiedScheduledFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DisconnectUnitTest {
  @Autowired
  private LobbyService lobbyService;
  @Autowired
  private RoomService roomService;

  @Test
  public void roomService_shouldTerminateMethodBecauseFoundRoomIsEmpty()
    throws IOException {
    Interlocutor user = new Human(null);

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doReturn(Optional.empty())
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(user));

    spiedRoomService.onUserDisconnect(user);

    Mockito.verify(spiedRoom, Mockito.times(0))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));
  }

  @Test
  public void roomService_shouldNotSendInterlocutorDisconnectedEventBecauseIsHumanReturnsFalseAndRemoveRoom()
    throws IOException {
    Interlocutor user = new Human(null);

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(user));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(user));

    spiedRoomService.onUserDisconnect(user);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(0))
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(user));
  }

  @Test
  public void roomService_shouldSendInterlocutorDisconnectedEventAndRemoveRoom() throws IOException {
    Interlocutor user = new Human(null);

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(user));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(user));

    spiedRoomService.onUserDisconnect(user);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(user));
  }

  @Test
  public void lobbyService_shouldRemoveUserFromLobbyAndRemoveScheduledTask() {
    Interlocutor user = new Human(null);

    SpiedScheduledFuture spiedScheduledTask = Mockito.spy(new SpiedScheduledFuture());
    Mockito.doReturn(true)
      .when(spiedScheduledTask)
      .cancel(ArgumentMatchers.eq(false));

    Map<Human, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(lobbyService, "scheduledTasks", spiedScheduledTasks);
    Mockito.doReturn(spiedScheduledTask)
      .when(spiedScheduledTasks)
      .remove(ArgumentMatchers.eq(user));

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(true)
      .when(spiedLobby)
      .remove(ArgumentMatchers.eq(user));

    lobbyService.onUserDisconnect(user);

    Mockito.verify(spiedLobby, Mockito.times(1))
      .remove(ArgumentMatchers.eq(user));

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .remove(ArgumentMatchers.eq(user));

    Mockito.verify(spiedScheduledTask, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void lobbyService_shouldNotRemoveUserBecauseDoesNotExist() {
    Interlocutor user = new Human(null);

    Map<Human, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(lobbyService, "scheduledTasks", spiedScheduledTasks);
    Mockito.doReturn(null)
      .when(spiedScheduledTasks)
      .remove(ArgumentMatchers.eq(user));

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(false)
      .when(spiedLobby)
      .remove(ArgumentMatchers.eq(user));

    lobbyService.onUserDisconnect(user);

    Mockito.verify(spiedLobby, Mockito.times(1))
      .remove(ArgumentMatchers.eq(user));

    Mockito.verify(spiedScheduledTasks, Mockito.times(0))
      .remove(ArgumentMatchers.eq(user));
  }
}
