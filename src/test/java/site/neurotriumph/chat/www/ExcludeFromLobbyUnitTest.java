package site.neurotriumph.chat.www;

import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.storage.LobbyStorage;
import site.neurotriumph.chat.www.util.MockedWebSocketSession;
import site.neurotriumph.chat.www.util.SpiedScheduledFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExcludeFromLobbyUnitTest {
  @Autowired
  private LobbyService lobbyService;
  @MockBean
  private LobbyStorage lobbyStorage;

  @Before
  public void before() {
    ReflectionTestUtils.setField(lobbyService, "lobbyStorage", lobbyStorage);
  }

  @Test
  public void shouldCancelTaskAndReturnTrueBecauseTaskIsNotNull() {
    Interlocutor user = new Human(new MockedWebSocketSession());

    SpiedScheduledFuture spiedScheduledTask = Mockito.spy(new SpiedScheduledFuture());
    Mockito.doReturn(true)
      .when(spiedScheduledTask)
      .cancel(ArgumentMatchers.eq(false));

    Mockito.doReturn(spiedScheduledTask)
      .when(lobbyStorage)
      .exclude(ArgumentMatchers.eq(user));

    assertTrue(lobbyService.excludeFromLobby(user));

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(user));

    Mockito.verify(spiedScheduledTask, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void shouldNotCancelTaskAndReturnFalseBecauseTaskIsNull() {
    Interlocutor user = new Human(new MockedWebSocketSession());

    Mockito.doReturn(null)
      .when(lobbyStorage)
      .exclude(ArgumentMatchers.eq(user));

    assertFalse(lobbyService.excludeFromLobby(user));

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .exclude(ArgumentMatchers.eq(user));
  }
}
