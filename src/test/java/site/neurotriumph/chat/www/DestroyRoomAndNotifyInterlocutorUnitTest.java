package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
import site.neurotriumph.chat.www.util.MockedWebSocketSession;
import site.neurotriumph.chat.www.util.SpiedScheduledFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DestroyRoomAndNotifyInterlocutorUnitTest {
  @SpyBean
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
    Interlocutor user = new Human(new MockedWebSocketSession());

    Room spiedRoom = Mockito.spy(new Room(user, null));
    Mockito.doReturn(null)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doReturn(Optional.empty())
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(roomService, Mockito.times(0))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));
  }

  @Test
  public void shouldNotSendInterlocutorDisconnectedEventAndExcludeRoomAndCancelTask()
    throws IOException {
    Interlocutor user = new Human(new MockedWebSocketSession());

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(null));

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

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();
  }

  @Test
  public void shouldSendInterlocutorDisconnectedEventAndExcludeRoomAndNotCancelTask() throws IOException {
    Interlocutor user = new Human(new MockedWebSocketSession());

    Human spiedInterlocutor = Mockito.spy(new Human(new MockedWebSocketSession()));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .sendAndClose(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .sendAndClose(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));
  }

  @Test
  public void shouldSendInterlocutorDisconnectedEventAndExcludeRoomAndCancelTask() throws IOException {
    Interlocutor user = new Human(new MockedWebSocketSession());

    Human spiedInterlocutor = Mockito.spy(new Human(new MockedWebSocketSession()));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .sendAndClose(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Room spiedRoom = Mockito.spy(new Room(user, spiedInterlocutor));
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.destroyRoomAndNotifyInterlocutor(user);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(user));

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .sendAndClose(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));
  }
}
