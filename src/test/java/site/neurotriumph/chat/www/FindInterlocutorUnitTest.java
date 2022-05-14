package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;
import site.neurotriumph.chat.www.service.LobbyService;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.util.SpiedRandom;
import site.neurotriumph.chat.www.util.SpiedScheduledFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FindInterlocutorUnitTest {
  @Value("${app.lobby_spent_time}")
  private long lobby_spent_time;
  @Autowired
  private LobbyService lobbyService;
  @MockBean
  private RoomService roomService;
  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void afterSpentTimeInLobby_shouldCreateRoom() throws IOException {
    Human spiedHuman = Mockito.mock(Human.class);
    Mockito.doNothing()
      .when(spiedHuman)
      .send(ArgumentMatchers.eq(new Event(EventType.NO_ONE_TO_TALK)));

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(true)
      .when(spiedLobby)
      .contains(ArgumentMatchers.eq(spiedHuman));
    Mockito.doReturn(true)
      .when(spiedLobby)
      .remove(ArgumentMatchers.eq(spiedHuman));

    Map<Human, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(lobbyService, "scheduledTasks", spiedScheduledTasks);
    Mockito.doReturn(null)
      .when(spiedScheduledTasks)
      .remove(ArgumentMatchers.eq(spiedHuman));

    Mockito.when(neuralNetworkRepository.findOneRandom())
      .thenReturn(Optional.of(new NeuralNetwork()));

    lobbyService.afterSpentTimeInLobby(spiedHuman);

    Mockito.verify(spiedLobby, Mockito.times(1))
      .contains(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(spiedLobby, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();

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

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(true)
      .when(spiedLobby)
      .contains(ArgumentMatchers.eq(spiedHuman));
    Mockito.doReturn(true)
      .when(spiedLobby)
      .remove(ArgumentMatchers.eq(spiedHuman));

    Map<Human, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(lobbyService, "scheduledTasks", spiedScheduledTasks);
    Mockito.doReturn(null)
      .when(spiedScheduledTasks)
      .remove(ArgumentMatchers.eq(spiedHuman));

    Mockito.when(neuralNetworkRepository.findOneRandom())
      .thenReturn(Optional.empty());

    lobbyService.afterSpentTimeInLobby(spiedHuman);

    Mockito.verify(spiedLobby, Mockito.times(1))
      .contains(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(spiedLobby, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .remove(ArgumentMatchers.eq(spiedHuman));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();

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

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(false)
      .when(spiedLobby)
      .contains(ArgumentMatchers.eq(human));

    lobbyService.afterSpentTimeInLobby(human);

    Mockito.verify(spiedLobby, Mockito.times(0))
      .remove(ArgumentMatchers.eq(human));
  }

  @Test
  public void shouldAddHumanToLobbyThenScheduleRunnableAndReturnNullBecauseLobbySizeIsZero() throws IOException {
    Human human = new Human(null);

    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(lobbyService, "random", spiedRandom);
    Mockito.doReturn(1)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(0)
      .when(spiedLobby)
      .size();
    Mockito.doReturn(true)
      .when(spiedLobby)
      .add(ArgumentMatchers.eq(human));

    ScheduledExecutorService spiedExecutorService = Mockito.mock(ScheduledExecutorService.class);
    ReflectionTestUtils.setField(lobbyService, "executorService", spiedExecutorService);
    Mockito.doReturn(null)
      .when(spiedExecutorService)
      .schedule(
        ArgumentMatchers.any(Runnable.class),
        ArgumentMatchers.eq(lobby_spent_time),
        ArgumentMatchers.eq(TimeUnit.MILLISECONDS));

    Interlocutor interlocutor = lobbyService.findInterlocutor(human);
    assertNull(interlocutor);

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedLobby, Mockito.times(1))
      .size();

    Mockito.verify(spiedLobby, Mockito.times(1))
      .add(ArgumentMatchers.eq(human));

    Mockito.verify(spiedExecutorService, Mockito.times(1))
      .schedule(
        ArgumentMatchers.any(Runnable.class),
        ArgumentMatchers.eq(lobby_spent_time),
        ArgumentMatchers.eq(TimeUnit.MILLISECONDS));
  }

  @Test
  public void shouldReturnHumanBecauseLobbySizeIsNotZero() throws IOException {
    Human foundHuman = new Human(null);

    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(lobbyService, "random", spiedRandom);
    Mockito.doReturn(1)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(1)
      .when(spiedLobby)
      .size();
    Mockito.doReturn(foundHuman)
      .when(spiedLobby)
      .remove(ArgumentMatchers.eq(0));

    SpiedScheduledFuture spiedScheduledFuture = Mockito.spy(new SpiedScheduledFuture());

    Map<Human, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(lobbyService, "scheduledTasks", spiedScheduledTasks);
    Mockito.doReturn(spiedScheduledFuture)
      .when(spiedScheduledTasks)
      .remove(ArgumentMatchers.eq(foundHuman));

    Interlocutor interlocutor = lobbyService.findInterlocutor(new Human(null));
    assertNotNull(interlocutor);
    assertTrue(interlocutor.isHuman());

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(spiedLobby, Mockito.times(1))
      .size();

    Mockito.verify(spiedLobby, Mockito.times(1))
      .remove(ArgumentMatchers.eq(0));

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .remove(ArgumentMatchers.eq(foundHuman));

    Mockito.verify(spiedScheduledFuture, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void shouldNotFindNeuralNetworkAndReturnHumanBecauseLobbySizeIsNotZero() throws IOException {
    Human foundHuman = new Human(null);

    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(lobbyService, "random", spiedRandom);
    Mockito.doReturn(0)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.when(neuralNetworkRepository.findOneRandom())
      .thenReturn(Optional.empty());

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);
    Mockito.doReturn(1)
      .when(spiedLobby)
      .size();
    Mockito.doReturn(foundHuman)
      .when(spiedLobby)
      .remove(ArgumentMatchers.eq(0));

    SpiedScheduledFuture spiedScheduledFuture = Mockito.spy(new SpiedScheduledFuture());

    Map<Human, ScheduledFuture<?>> spiedScheduledTasks = Mockito.spy(new HashMap<>());
    ReflectionTestUtils.setField(lobbyService, "scheduledTasks", spiedScheduledTasks);
    Mockito.doReturn(spiedScheduledFuture)
      .when(spiedScheduledTasks)
      .remove(ArgumentMatchers.eq(foundHuman));

    Interlocutor interlocutor = lobbyService.findInterlocutor(new Human(null));
    assertNotNull(interlocutor);
    assertTrue(interlocutor.isHuman());

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();

    Mockito.verify(spiedLobby, Mockito.times(2))
      .size();

    Mockito.verify(spiedLobby, Mockito.times(1))
      .remove(ArgumentMatchers.eq(0));

    Mockito.verify(spiedScheduledTasks, Mockito.times(1))
      .remove(ArgumentMatchers.eq(foundHuman));

    Mockito.verify(spiedScheduledFuture, Mockito.times(1))
      .cancel(ArgumentMatchers.eq(false));
  }

  @Test
  public void shouldNotFindNeuralNetworkAndReturnNullBecauseLobbySizeIsZero() throws IOException {
    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(lobbyService, "random", spiedRandom);
    Mockito.doReturn(0)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.when(neuralNetworkRepository.findOneRandom())
      .thenReturn(Optional.empty());

    List<Human> spiedLobby = Mockito.spy(new ArrayList<>());
    ReflectionTestUtils.setField(lobbyService, "lobby", spiedLobby);

    Human spiedHuman = Mockito.mock(Human.class);
    Mockito.doNothing()
      .when(spiedHuman)
      .send(ArgumentMatchers.eq(new Event(EventType.NO_ONE_TO_TALK)));

    Interlocutor interlocutor = lobbyService.findInterlocutor(spiedHuman);
    assertNull(interlocutor);

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();

    Mockito.verify(spiedLobby, Mockito.times(1))
      .size();

    Mockito.verify(spiedHuman, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.NO_ONE_TO_TALK)));
  }

  @Test
  public void shouldFindNeuralNetworkAndReturnIt() throws IOException {
    SpiedRandom spiedRandom = Mockito.spy(new SpiedRandom());
    ReflectionTestUtils.setField(lobbyService, "random", spiedRandom);
    Mockito.doReturn(0)
      .when(spiedRandom)
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.when(neuralNetworkRepository.findOneRandom())
      .thenReturn(Optional.of(new NeuralNetwork()));

    Interlocutor interlocutor = lobbyService.findInterlocutor(new Human(null));
    assertNotNull(interlocutor);
    assertFalse(interlocutor.isHuman());

    Mockito.verify(spiedRandom, Mockito.times(1))
      .nextInt(ArgumentMatchers.eq(2));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();
  }
}
