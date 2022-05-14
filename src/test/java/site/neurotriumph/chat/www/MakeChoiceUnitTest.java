package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.Choice;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.MakeChoiceEvent;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MakeChoiceUnitTest {
  @Value("${app.required_number_of_messages_to_make_a_choice}")
  public int required_number_of_messages_to_make_a_choice;
  @Autowired
  private RoomService roomService;

  @Test
  public void shouldSendItWasMachineEventBecauseChoiceEqualsIdkAndIsHumanMethodReturnsFalse()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.IDK)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(null));

    Interlocutor spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_MACHINE)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(required_number_of_messages_to_make_a_choice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnother(ArgumentMatchers.eq(spiedSender));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(spiedSender));

    spiedRoomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnother(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(2))
      .isHuman();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(1))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_MACHINE)));
  }

  @Test
  public void shouldSendItWasHumanEventBecauseChoiceEqualsIdkAndIsHumanMethodReturnsTrue()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.IDK)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Interlocutor spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_HUMAN)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(required_number_of_messages_to_make_a_choice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnother(ArgumentMatchers.eq(spiedSender));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(spiedSender));

    spiedRoomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnother(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(2))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(1))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_HUMAN)));
  }

  @Test
  public void shouldSendYouAreRightEventBecauseChoiceEqualsItsAMachineAndIsHumanMethodReturnsFalse()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_MACHINE)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(null));

    Interlocutor spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(required_number_of_messages_to_make_a_choice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnother(ArgumentMatchers.eq(spiedSender));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(spiedSender));

    spiedRoomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnother(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(2))
      .isHuman();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(3))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));
  }

  @Test
  public void shouldSendYouAreRightEventBecauseChoiceEqualsItsAHumanAndIsHumanMethodReturnsTrue()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_HUMAN)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Interlocutor spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(required_number_of_messages_to_make_a_choice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnother(ArgumentMatchers.eq(spiedSender));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(spiedSender));

    spiedRoomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnother(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(2))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(2))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));
  }

  @Test
  public void shouldSendYouAreWrongEventBecauseChoiceEqualsItsAHumanAndIsHumanMethodReturnsFalse()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_HUMAN)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(null));

    Interlocutor spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(required_number_of_messages_to_make_a_choice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnother(ArgumentMatchers.eq(spiedSender));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(spiedSender));

    spiedRoomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnother(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(2))
      .isHuman();

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(3))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));
  }

  @Test
  public void shouldSendYouAreWrongEventBecauseChoiceEqualsItsAMachineAndIsHumanMethodReturnsTrue()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_MACHINE)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Interlocutor spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Interlocutor spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(required_number_of_messages_to_make_a_choice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnother(ArgumentMatchers.eq(spiedSender));

    List<Room> spiedRooms = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(roomService, "rooms", spiedRooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));
    Mockito.doReturn(Optional.of(spiedRoom))
      .when(spiedRoomService)
      .findRoom(ArgumentMatchers.eq(spiedSender));

    spiedRoomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnother(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(2))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Mockito.verify(spiedRooms, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .removeAndCancelScheduledTask(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(3))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));
  }
}
