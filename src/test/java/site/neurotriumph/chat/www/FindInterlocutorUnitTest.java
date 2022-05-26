package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.storage.LobbyStorage;
import site.neurotriumph.chat.www.util.SpiedRandom;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FindInterlocutorUnitTest {
  @Value("${app.lobby_spent_time}")
  private long lobbySpentTime;
  @SpyBean
  private LobbyService lobbyService;
  @MockBean
  private RoomService roomService;
  @MockBean
  private LobbyStorage lobbyStorage;
  @MockBean
  @Qualifier("spiedRandom")
  private SpiedRandom random;
  @MockBean
  private ScheduledExecutorService scheduledExecutorService;

  @Before
  public void before() {
    ReflectionTestUtils.setField(lobbyService, "random", random);
    ReflectionTestUtils.setField(lobbyService, "lobbyStorage", lobbyStorage);
    ReflectionTestUtils.setField(lobbyService, "scheduledExecutorService", scheduledExecutorService);
  }

  @Test
  public void afterSpentTimeInLobby_shouldCreateRoom() throws IOException {
    Human spiedHuman = Mockito.mock(Human.class);

    Mockito.doReturn(true)
      .when(lobbyService)
      .excludeFromLobby(ArgumentMatchers.eq(spiedHuman));

    Mockito.doReturn(new Machine(null))
      .when(lobbyService)
      .findMachine();

    lobbyService.afterSpentTimeInLobby(spiedHuman);

    Mockito.verify(lobbyService, Mockito.times(1))
      .excludeFromLobby(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(lobbyService, Mockito.times(1))
      .findMachine();

    Mockito.verify(spiedHuman, Mockito.times(0))
      .send(ArgumentMatchers.eq(new Event(EventType.NO_ONE_TO_TALK)));

    Mockito.verify(roomService, Mockito.times(1))
      .create(
        ArgumentMatchers.eq(spiedHuman),
        ArgumentMatchers.any(Machine.class));
  }

  @Test
  public void afterSpentTimeInLobby_shouldTerminateMethodBecauseNeuralNetworkIsEmpty() throws IOException {
    Human spiedHuman = Mockito.mock(Human.class);
    Mockito.doNothing()
      .when(spiedHuman)
      .send(ArgumentMatchers.eq(new Event(EventType.NO_ONE_TO_TALK)));

    Mockito.doReturn(true)
      .when(lobbyService)
      .excludeFromLobby(ArgumentMatchers.eq(spiedHuman));

    Mockito.doReturn(null)
      .when(lobbyService)
      .findMachine();

    lobbyService.afterSpentTimeInLobby(spiedHuman);

    Mockito.verify(lobbyService, Mockito.times(1))
      .excludeFromLobby(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(lobbyService, Mockito.times(1))
      .findMachine();

    Mockito.verify(spiedHuman, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.NO_ONE_TO_TALK)));

    Mockito.verify(roomService, Mockito.times(0))
      .create(
        ArgumentMatchers.eq(spiedHuman),
        ArgumentMatchers.any(Machine.class));
  }

  @Test
  public void afterSpentTimeInLobby_shouldTerminateMethodBecauseLobbyNotContainsUser() throws IOException {
    Human human = new Human(null);

    Mockito.doReturn(false)
      .when(lobbyService)
      .excludeFromLobby(ArgumentMatchers.eq(human));

    lobbyService.afterSpentTimeInLobby(human);

    Mockito.verify(lobbyService, Mockito.times(0))
      .findMachine();
  }

  @Test
  public void shouldAddHumanToLobbyThenScheduleRunnableAndReturnNullBecauseLobbySizeIsZero() {
    Human human = new Human(null);

    Mockito.doReturn(1)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.doReturn(0)
      .when(lobbyStorage)
      .size();
    Mockito.doNothing()
      .when(lobbyStorage)
      .add(
        ArgumentMatchers.eq(human),
        ArgumentMatchers.eq(null));

    Mockito.doReturn(null)
      .when(scheduledExecutorService)
      .schedule(
        ArgumentMatchers.any(Runnable.class),
        ArgumentMatchers.eq(lobbySpentTime),
        ArgumentMatchers.eq(TimeUnit.MILLISECONDS));

    Interlocutor interlocutor = lobbyService.findInterlocutor(human);
    assertNull(interlocutor);

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .size();

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .add(
        ArgumentMatchers.eq(human),
        ArgumentMatchers.eq(null));

    Mockito.verify(scheduledExecutorService, Mockito.times(1))
      .schedule(
        ArgumentMatchers.any(Runnable.class),
        ArgumentMatchers.eq(lobbySpentTime),
        ArgumentMatchers.eq(TimeUnit.MILLISECONDS));
  }

  @Test
  public void shouldReturnHumanBecauseLobbySizeIsNotZero() {
    Human foundHuman = new Human(null);

    Mockito.doReturn(1)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.doReturn(1)
      .when(lobbyStorage)
      .size();
    Mockito.doReturn(foundHuman)
      .when(lobbyStorage)
      .getFirst();

    Mockito.doReturn(true)
      .when(lobbyService)
      .excludeFromLobby(ArgumentMatchers.eq(foundHuman));
    Mockito.doReturn(null)
      .when(lobbyService)
      .findMachine();

    Interlocutor interlocutor = lobbyService.findInterlocutor(new Human(null));
    assertNotNull(interlocutor);
    assertTrue(interlocutor.isHuman());

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .size();

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .getFirst();

    Mockito.verify(lobbyService, Mockito.times(1))
      .excludeFromLobby(ArgumentMatchers.eq(foundHuman));
  }

  @Test
  public void shouldNotFindNeuralNetworkAndReturnHumanBecauseLobbySizeIsNotZero() {
    Human foundHuman = new Human(null);

    Mockito.doReturn(0)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.doReturn(null)
      .when(lobbyService)
      .findMachine();

    Mockito.doReturn(1)
      .when(lobbyStorage)
      .size();
    Mockito.doReturn(foundHuman)
      .when(lobbyStorage)
      .getFirst();

    Mockito.doReturn(true)
      .when(lobbyService)
      .excludeFromLobby(ArgumentMatchers.eq(foundHuman));

    Interlocutor interlocutor = lobbyService.findInterlocutor(new Human(null));
    assertNotNull(interlocutor);
    assertTrue(interlocutor.isHuman());

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(lobbyService, Mockito.times(1))
      .findMachine();

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .size();

    Mockito.verify(lobbyStorage, Mockito.times(1))
      .getFirst();

    Mockito.verify(lobbyService, Mockito.times(1))
      .excludeFromLobby(ArgumentMatchers.eq(foundHuman));
  }

  @Test
  public void shouldFindNeuralNetworkAndReturnIt() {
    Mockito.doReturn(0)
      .when(random)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.doReturn(new Machine(null))
      .when(lobbyService)
      .findMachine();

    Interlocutor interlocutor = lobbyService.findInterlocutor(new Human(null));
    assertNotNull(interlocutor);
    assertFalse(interlocutor.isHuman());

    Mockito.verify(random, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(lobbyService, Mockito.times(1))
      .findMachine();
  }
}
