package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.DisconnectReason;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.storage.RoomStorage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SendMachineResponseUnitTest {
  @Value("${app.chat_messaging_delay}")
  private long chatMessagingDelay;
  @SpyBean
  private RoomService roomService;
  @MockBean
  private RoomStorage roomStorage;

  @Before
  public void before() {
    ReflectionTestUtils.setField(roomService, "roomStorage", roomStorage);
  }

  @Test
  public void shouldSendEventAndTerminateMethodBecauseResponseIsNull() throws IOException {
    Human spiedUser = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedUser)
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(null));

    roomService.sendMachineResponse(null, spiedUser, null);

    Mockito.verify(spiedUser, Mockito.times(1))
      .send(ArgumentMatchers.eq(new DisconnectEvent(DisconnectReason.INTERLOCUTOR_DISCONNECTED)));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(null));
  }

  @Test
  public void shouldScheduleTaskAndPutItIntoScheduledTasksThanSendMessageAndUpdateRoom()
    throws IOException, InterruptedException {
    ReflectionTestUtils.setField(roomService, "chatMessagingDelay", 0);

    ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    Room spiedRoom = Mockito.spy(new Room(null, null));

    Interlocutor spiedUser = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedUser)
      .send(ArgumentMatchers.eq(chatMessageEvent));

    roomService.sendMachineResponse(chatMessageEvent, spiedUser, spiedRoom);

    Thread.sleep(1000);

    Mockito.verify(roomStorage, Mockito.times(1))
      .addTask(
        ArgumentMatchers.eq(spiedRoom),
        ArgumentMatchers.any(ScheduledFuture.class));

    Mockito.verify(spiedUser, Mockito.times(1))
      .send(ArgumentMatchers.eq(chatMessageEvent));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .updateTimePoint();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .increaseMessageCounter();

    ReflectionTestUtils.setField(roomService, "chatMessagingDelay", chatMessagingDelay);
  }

  @Test
  public void shouldScheduleTaskAndPutItIntoScheduledTasks() throws IOException {
    roomService.sendMachineResponse(new ChatMessageEvent(), null, null);

    Mockito.verify(roomStorage, Mockito.times(1))
      .addTask(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.any(ScheduledFuture.class));
  }
}
