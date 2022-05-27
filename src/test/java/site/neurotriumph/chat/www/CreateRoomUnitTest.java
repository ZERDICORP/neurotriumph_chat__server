package site.neurotriumph.chat.www;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.storage.RoomStorage;
import site.neurotriumph.chat.www.util.SpiedExecutorService;
import site.neurotriumph.chat.www.util.SpiedRandom;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateRoomUnitTest {
  @SpyBean
  private RoomService roomService;
  @SpyBean
  private SpiedExecutorService executorService;
  @MockBean
  @Qualifier("spiedRandom")
  private SpiedRandom random;
  @MockBean
  private RoomStorage roomStorage;

  @Before
  public void before() {
    ReflectionTestUtils.setField(roomService, "random", random);
    ReflectionTestUtils.setField(roomService, "executorService", executorService);
    ReflectionTestUtils.setField(roomService, "roomStorage", roomStorage);
  }

  @Test
  public void shouldCreateRoomAndSendMessageAndThanTerminateMethodBecauseRandIsEquals0AndIsHumanMethodReturnsFalse()
    throws IOException {
    Mockito.doReturn(0)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.doNothing()
      .when(executorService)
      .execute(ArgumentMatchers.any(Runnable.class));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Machine(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    Mockito.doReturn(spiedRoom)
      .when(roomStorage)
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Event eventForSecondInterlocutor = new Event(EventType.INIT_CHAT_MESSAGE);
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(eventForSecondInterlocutor));

    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.any(InterlocutorFoundEvent.class));

    Mockito.doNothing()
      .when(roomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(roomStorage, Mockito.times(1))
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(0))
      .swapInterlocutors();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(InterlocutorFoundEvent.class));

    Mockito.verify(executorService, Mockito.times(0))
      .execute(ArgumentMatchers.any(Runnable.class));
  }

  @Test
  public void shouldCreateRoomAndSendMessagesButRandIsEquals1AndIsHumanMethodReturnsFalse()
    throws IOException, InterruptedException {
    Mockito.doReturn(1)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Machine spiedSecondInterlocutor = Mockito.spy(new Machine(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    Mockito.doReturn(spiedRoom)
      .when(roomStorage)
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(new Event(EventType.INIT_CHAT_MESSAGE)));

    InterlocutorFoundEvent interlocutorFoundEventForFirstInterlocutor = new InterlocutorFoundEvent(false);
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.doNothing()
      .when(roomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Thread.sleep(1000);

    Mockito.verify(roomStorage, Mockito.times(1))
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.verify(executorService, Mockito.times(1))
      .execute(ArgumentMatchers.any(Runnable.class));
  }

  @Test
  public void shouldCreateRoomAndSendMessagesAndThanTerminateMethodBecauseRandIsEquals1AndIsHumanMethodReturnsTrue()
    throws IOException {
    Mockito.doReturn(1)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    Mockito.doReturn(spiedRoom)
      .when(roomStorage)
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.any(InterlocutorFoundEvent.class));

    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.any(InterlocutorFoundEvent.class));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(roomStorage, Mockito.times(1))
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(InterlocutorFoundEvent.class));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(InterlocutorFoundEvent.class));
  }

  @Test
  public void shouldCreateRoomAndSendMessagesAndThanTerminateMethodBecauseRandIsEquals0AndIsHumanMethodReturnsTrue()
    throws IOException {
    Mockito.doReturn(0)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    Mockito.doReturn(spiedRoom)
      .when(roomStorage)
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    InterlocutorFoundEvent interlocutorFoundEventForSecondInterlocutor = new InterlocutorFoundEvent(false);
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForSecondInterlocutor));

    InterlocutorFoundEvent interlocutorFoundEventForFirstInterlocutor = new InterlocutorFoundEvent(true);
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(roomStorage, Mockito.times(1))
      .createNew(
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedSecondInterlocutor));

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(0))
      .swapInterlocutors();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForSecondInterlocutor));
  }
}
