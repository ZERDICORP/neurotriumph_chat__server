package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.InterlocutorFoundEvent;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.util.SpiedRandom;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateRoomUnitTest {
  @Autowired
  private RoomService roomService;

  @Test
  public void shouldCreateRoomAndSendMessageAndThanTerminateMethodBecauseRandIsEquals0AndIsHumanMethodReturnsFalse()
    throws IOException {
    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(roomService, "random", spiedRandom);
    Mockito.doReturn(0)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Machine(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);
    Mockito.doReturn(true)
      .when(spiedRooms)
      .add(ArgumentMatchers.any(Room.class));
    Mockito.doReturn(1)
      .when(spiedRooms)
      .size();
    Mockito.doReturn(spiedRoom)
      .when(spiedRooms)
      .get(ArgumentMatchers.eq(0));

    Event eventForSecondInterlocutor = new Event(EventType.INIT_CHAT_MESSAGE);
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(eventForSecondInterlocutor));

    InterlocutorFoundEvent interlocutorFoundEventForFirstInterlocutor = new InterlocutorFoundEvent(
      room.getTimePoint(), true);
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(spiedRooms, Mockito.times(1))
      .add(ArgumentMatchers.any(Room.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .size();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .get(ArgumentMatchers.eq(0));

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(0))
      .swapInterlocutors();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getTimePoint();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(0))
      .send(ArgumentMatchers.eq(eventForSecondInterlocutor));
  }

  @Test
  public void shouldCreateRoomAndSendMessagesButRandIsEquals1AndIsHumanMethodReturnsFalse() throws IOException {
    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(roomService, "random", spiedRandom);
    Mockito.doReturn(1)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Machine(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);
    Mockito.doReturn(true)
      .when(spiedRooms)
      .add(ArgumentMatchers.any(Room.class));
    Mockito.doReturn(1)
      .when(spiedRooms)
      .size();
    Mockito.doReturn(spiedRoom)
      .when(spiedRooms)
      .get(ArgumentMatchers.eq(0));

    Event eventForSecondInterlocutor = new Event(EventType.INIT_CHAT_MESSAGE);
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(eventForSecondInterlocutor));

    InterlocutorFoundEvent interlocutorFoundEventForFirstInterlocutor = new InterlocutorFoundEvent(
      room.getTimePoint(), false);
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    spiedRoomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(spiedRooms, Mockito.times(1))
      .add(ArgumentMatchers.any(Room.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .size();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .get(ArgumentMatchers.eq(0));

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getTimePoint();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(eventForSecondInterlocutor));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));
  }

  @Test
  public void shouldCreateRoomAndSendMessagesAndThanTerminateMethodBecauseRandIsEquals1AndIsHumanMethodReturnsTrue()
    throws IOException {
    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(roomService, "random", spiedRandom);
    Mockito.doReturn(1)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);
    Mockito.doReturn(true)
      .when(spiedRooms)
      .add(ArgumentMatchers.any(Room.class));
    Mockito.doReturn(1)
      .when(spiedRooms)
      .size();
    Mockito.doReturn(spiedRoom)
      .when(spiedRooms)
      .get(ArgumentMatchers.eq(0));

    InterlocutorFoundEvent interlocutorFoundEventForSecondInterlocutor = new InterlocutorFoundEvent(
      room.getTimePoint(), true);
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForSecondInterlocutor));

    InterlocutorFoundEvent interlocutorFoundEventForFirstInterlocutor = new InterlocutorFoundEvent(
      room.getTimePoint(), false);
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(spiedRooms, Mockito.times(1))
      .add(ArgumentMatchers.any(Room.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .size();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .get(ArgumentMatchers.eq(0));

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedRoom, Mockito.times(2))
      .getTimePoint();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForSecondInterlocutor));
  }

  @Test
  public void shouldCreateRoomAndSendMessagesAndThanTerminateMethodBecauseRandIsEquals0AndIsHumanMethodReturnsTrue()
    throws IOException {
    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(roomService, "random", spiedRandom);
    Mockito.doReturn(0)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));

    Room room = new Room(spiedFirstInterlocutor, spiedSecondInterlocutor);
    Room spiedRoom = Mockito.spy(room);

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);
    Mockito.doReturn(true)
      .when(spiedRooms)
      .add(ArgumentMatchers.any(Room.class));
    Mockito.doReturn(1)
      .when(spiedRooms)
      .size();
    Mockito.doReturn(spiedRoom)
      .when(spiedRooms)
      .get(ArgumentMatchers.eq(0));

    InterlocutorFoundEvent interlocutorFoundEventForSecondInterlocutor = new InterlocutorFoundEvent(
      room.getTimePoint(), false);
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForSecondInterlocutor));

    InterlocutorFoundEvent interlocutorFoundEventForFirstInterlocutor = new InterlocutorFoundEvent(
      room.getTimePoint(), true);
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    roomService.create(spiedFirstInterlocutor, spiedSecondInterlocutor);

    Mockito.verify(spiedRooms, Mockito.times(1))
      .add(ArgumentMatchers.any(Room.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .size();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .get(ArgumentMatchers.eq(0));

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedRoom, Mockito.times(0))
      .swapInterlocutors();

    Mockito.verify(spiedRoom, Mockito.times(2))
      .getTimePoint();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForFirstInterlocutor));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(interlocutorFoundEventForSecondInterlocutor));
  }
}
