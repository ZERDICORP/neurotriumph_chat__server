package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.HashMap;
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
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SendMachineResponseUnitTest {
  @Autowired
  private RoomService roomService;

  @Test
  public void shouldScheduleTaskAndPutItIntoScheduledTasksThanSendMessageAndUpdateRoom() throws IOException,
    InterruptedException {
    ReflectionTestUtils.setField(roomService, "chatMessagingDelay", 0);

    Room spiedRoom = Mockito.spy(new Room(null, null));

    Interlocutor spiedUser = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedUser)
      .send(ArgumentMatchers.eq(null));

    HashMap<Interlocutor, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(roomService, "scheduledTasks", spiedScheduledTasks);

    roomService.sendMachineResponse(null, spiedUser, spiedRoom);

    Thread.sleep(1000);

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .put(
        ArgumentMatchers.eq(spiedUser),
        ArgumentMatchers.any(ScheduledFuture.class));

    Mockito.verify(spiedUser, Mockito.times(1))
      .send(ArgumentMatchers.eq(null));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .updateTimePoint();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .swapInterlocutors();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .increaseMessageCounter();
  }

  @Test
  public void shouldScheduleTaskAndPutItIntoScheduledTasks() {
    HashMap<Interlocutor, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(roomService, "scheduledTasks", spiedScheduledTasks);

    roomService.sendMachineResponse(null, null, null);

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .put(
        ArgumentMatchers.eq(null),
        ArgumentMatchers.any(ScheduledFuture.class));
  }
}
