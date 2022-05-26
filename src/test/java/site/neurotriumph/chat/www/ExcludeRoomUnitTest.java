package site.neurotriumph.chat.www;

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
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.storage.RoomStorage;
import site.neurotriumph.chat.www.util.SpiedScheduledFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExcludeRoomUnitTest {
  @Autowired
  private RoomService roomService;
  @MockBean
  private RoomStorage roomStorage;

  @Before
  public void before() {
    ReflectionTestUtils.setField(roomService, "roomStorage", roomStorage);
  }

  @Test
  public void shouldCancelTaskAndReturnTrueBecauseTaskIsNotNull() {
    SpiedScheduledFuture spiedScheduledTask = Mockito.spy(new SpiedScheduledFuture());
    Mockito.doReturn(true)
      .when(spiedScheduledTask)
      .cancel(ArgumentMatchers.eq(false));

    Mockito.doReturn(spiedScheduledTask)
      .when(roomStorage)
      .exclude(ArgumentMatchers.eq(null));

    roomService.excludeRoom(null);

    Mockito.verify(roomStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(null));

    Mockito.verify(spiedScheduledTask, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void shouldNotCancelTaskBecauseTaskIsNull() {
    Mockito.doReturn(null)
      .when(roomStorage)
      .exclude(ArgumentMatchers.eq(null));

    roomService.excludeRoom(null);

    Mockito.verify(roomStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(null));
  }
}
