package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.storage.RoomStorage;
import site.neurotriumph.chat.www.util.SpiedScheduledFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DestroyRoomAndNotifyInterlocutorUnitTest {
  @Autowired
  private RoomService roomService;
  @MockBean
  private RoomStorage roomStorage;

  @Before
  public void before() {
    ReflectionTestUtils.setField(roomService, "roomStorage", roomStorage);
  }

  @Test
  public void shouldTerminateMethodBecauseFoundRoomIsEmpty()
    throws IOException {
    Interlocutor user = new Human(null);

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doReturn(Optional.empty())
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedRoom, Mockito.times(0))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));
  }

  @Test
  public void shouldNotSendInterlocutorDisconnectedEventAndExcludeRoomAndCancelTask()
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

    SpiedScheduledFuture spiedScheduledTask = Mockito.spy(new SpiedScheduledFuture());
    Mockito.doReturn(true)
      .when(spiedScheduledTask)
      .cancel(ArgumentMatchers.eq(false));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));
    Mockito.doReturn(spiedScheduledTask)
      .when(roomStorage)
      .exclude(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(0))
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.verify(roomStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedScheduledTask, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void shouldSendInterlocutorDisconnectedEventAndExcludeRoomAndNotCancelTask() throws IOException {
    Interlocutor user = new Human(null);

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    SpiedScheduledFuture spiedScheduledTask = Mockito.spy(new SpiedScheduledFuture());
    Mockito.doReturn(true)
      .when(spiedScheduledTask)
      .cancel(ArgumentMatchers.eq(false));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));
    Mockito.doReturn(null)
      .when(roomStorage)
      .exclude(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.verify(roomStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedScheduledTask, Mockito.times(0))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void shouldSendInterlocutorDisconnectedEventAndExcludeRoomAndCancelTask() throws IOException {
    Interlocutor user = new Human(null);

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    SpiedScheduledFuture spiedScheduledTask = Mockito.spy(new SpiedScheduledFuture());
    Mockito.doReturn(true)
      .when(spiedScheduledTask)
      .cancel(ArgumentMatchers.eq(false));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));
    Mockito.doReturn(spiedScheduledTask)
      .when(roomStorage)
      .exclude(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.verify(roomStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedScheduledTask, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }
}
