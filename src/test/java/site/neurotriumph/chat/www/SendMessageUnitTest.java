package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SendMessageUnitTest {
  @Value("${app.chat_messaging_delay}")
  public long chat_messaging_delay;
  @Autowired
  private RoomService roomService;

  @Test
  public void shouldTerminateFunctionBecauseFoundRoomIsEmpty() throws IOException {
    ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Date spiedTimePoint = Mockito.spy(new Date());
    Mockito.doReturn(new Date().getTime())
      .when(spiedTimePoint)
      .getTime();

    Room spiedRoom = Mockito.spy(new Room(spiedFirstInterlocutor, spiedSecondInterlocutor));
    Mockito.doReturn(spiedTimePoint)
      .when(spiedRoom)
      .getTimePoint();

    List<Room> rooms = new ArrayList<>();
    rooms.add(spiedRoom);
    ReflectionTestUtils.setField(roomService, "rooms", rooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    spiedRoomService.sendMessage(new Human(null), chatMessageEvent);

    Mockito.verify(spiedRoom, Mockito.times(0))
      .getFirstInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(0))
      .getSecondInterlocutor();
  }

  @Test
  public void shouldTerminateFunctionBecauseFirstInterlocutorNotEqualsSender() throws IOException {
    ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Date spiedTimePoint = Mockito.spy(new Date());
    Mockito.doReturn(new Date().getTime())
      .when(spiedTimePoint)
      .getTime();

    Room spiedRoom = Mockito.spy(new Room(spiedFirstInterlocutor, spiedSecondInterlocutor));
    Mockito.doReturn(spiedTimePoint)
      .when(spiedRoom)
      .getTimePoint();

    List<Room> rooms = new ArrayList<>();
    rooms.add(spiedRoom);
    ReflectionTestUtils.setField(roomService, "rooms", rooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    spiedRoomService.sendMessage(spiedSecondInterlocutor, chatMessageEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getFirstInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getSecondInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(0))
      .getTimePoint();

    Mockito.verify(spiedTimePoint, Mockito.times(0))
      .getTime();
  }

  @Test
  public void shouldTerminateFunctionBecauseTimeDifferenceLessThanChatMessagingDelay() throws IOException {
    ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Date spiedTimePoint = Mockito.spy(new Date());
    Mockito.doReturn(new Date().getTime())
      .when(spiedTimePoint)
      .getTime();

    Room spiedRoom = Mockito.spy(new Room(spiedFirstInterlocutor, spiedSecondInterlocutor));
    Mockito.doReturn(spiedTimePoint)
      .when(spiedRoom)
      .getTimePoint();

    List<Room> rooms = new ArrayList<>();
    rooms.add(spiedRoom);
    ReflectionTestUtils.setField(roomService, "rooms", rooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    spiedRoomService.sendMessage(spiedFirstInterlocutor, chatMessageEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getFirstInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getSecondInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getTimePoint();

    Mockito.verify(spiedTimePoint, Mockito.times(1))
      .getTime();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(0))
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(0))
      .send(ArgumentMatchers.eq(chatMessageEvent));
  }

  @Test
  public void shouldSendMessagesButIsHumanMethodReturnsFalse() throws IOException {
    ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    Machine spiedSecondInterlocutor = Mockito.spy(new Machine(null));
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Date spiedTimePoint = Mockito.spy(new Date());
    Mockito.doReturn(new Date().getTime() - chat_messaging_delay)
      .when(spiedTimePoint)
      .getTime();

    Room spiedRoom = Mockito.spy(new Room(spiedFirstInterlocutor, spiedSecondInterlocutor));
    Mockito.doReturn(spiedTimePoint)
      .when(spiedRoom)
      .getTimePoint();

    List<Room> rooms = new ArrayList<>();
    rooms.add(spiedRoom);
    ReflectionTestUtils.setField(roomService, "rooms", rooms);

    RoomService spiedRoomService = Mockito.spy(roomService);
    Mockito.doNothing()
      .when(spiedRoomService)
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));

    spiedRoomService.sendMessage(spiedFirstInterlocutor, chatMessageEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getFirstInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getSecondInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getTimePoint();

    Mockito.verify(spiedTimePoint, Mockito.times(1))
      .getTime();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedRoomService, Mockito.times(1))
      .sendMachineResponse(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.eq(spiedFirstInterlocutor),
        ArgumentMatchers.eq(spiedRoom));
  }

  @Test
  public void shouldSendMessagesButIsHumanMethodReturnsTrue() throws IOException {
    ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    Interlocutor spiedSecondInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSecondInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Interlocutor spiedFirstInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedFirstInterlocutor)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Date spiedTimePoint = Mockito.spy(new Date());
    Mockito.doReturn(new Date().getTime() - chat_messaging_delay)
      .when(spiedTimePoint)
      .getTime();

    Room spiedRoom = Mockito.spy(new Room(spiedFirstInterlocutor, spiedSecondInterlocutor));
    Mockito.doReturn(spiedTimePoint)
      .when(spiedRoom)
      .getTimePoint();

    List<Room> rooms = new ArrayList<>();
    rooms.add(spiedRoom);
    ReflectionTestUtils.setField(roomService, "rooms", rooms);

    roomService.sendMessage(spiedFirstInterlocutor, chatMessageEvent);

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getFirstInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getSecondInterlocutor();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getTimePoint();

    Mockito.verify(spiedTimePoint, Mockito.times(1))
      .getTime();

    Mockito.verify(spiedFirstInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedSecondInterlocutor, Mockito.times(1))
      .isHuman();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .updateTimePoint();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .increaseMessageCounter();
  }
}
