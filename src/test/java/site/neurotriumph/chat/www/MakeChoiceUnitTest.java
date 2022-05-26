package site.neurotriumph.chat.www;

import java.io.IOException;
import java.util.Optional;
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
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Human;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.pojo.Choice;
import site.neurotriumph.chat.www.pojo.DisconnectEvent;
import site.neurotriumph.chat.www.pojo.Event;
import site.neurotriumph.chat.www.pojo.EventType;
import site.neurotriumph.chat.www.pojo.MakeChoiceEvent;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;
import site.neurotriumph.chat.www.room.Room;
import site.neurotriumph.chat.www.service.RoomService;
import site.neurotriumph.chat.www.storage.RoomStorage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MakeChoiceUnitTest {
  @Value("${app.required_number_of_messages_to_make_a_choice}")
  public int requiredNumberOfMessagesToMakeChoice;
  @SpyBean
  private RoomService roomService;
  @MockBean
  private RoomStorage roomStorage;
  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Before
  public void before() {
    ReflectionTestUtils.setField(roomService, "roomStorage", roomStorage);
  }

  @Test
  public void shouldTerminateMethodBecauseFoundRoomIsEmpty()
    throws IOException {
    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_MACHINE)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, null));

    Mockito.doReturn(Optional.empty())
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    roomService.makeChoice(spiedSender, null);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(0))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));
  }

  @Test
  public void shouldSendItWasMachineEventBecauseChoiceEqualsIdkAndIsHumanMethodReturnsFalse()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.IDK)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(new NeuralNetwork());

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(spiedNeuralNetwork));

    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_MACHINE)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(requiredNumberOfMessagesToMakeChoice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(3))
      .isHuman();

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(1))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_MACHINE)));

    Mockito.verify(spiedSender, Mockito.times(0))
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_HUMAN)));

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .incrementTests_passed();

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .save(ArgumentMatchers.eq(spiedNeuralNetwork));
  }

  @Test
  public void shouldSendItWasHumanEventBecauseChoiceEqualsIdkAndIsHumanMethodReturnsTrue()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.IDK)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Human spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_HUMAN)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(requiredNumberOfMessagesToMakeChoice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(3))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(1))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_HUMAN)));

    Mockito.verify(spiedSender, Mockito.times(0))
      .send(ArgumentMatchers.eq(new Event(EventType.IT_WAS_A_MACHINE)));

    Mockito.verify(neuralNetworkRepository, Mockito.times(0))
      .save(ArgumentMatchers.any(NeuralNetwork.class));
  }

  @Test
  public void shouldSendYouAreRightEventBecauseChoiceEqualsItsAMachineAndIsHumanMethodReturnsFalse()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_MACHINE)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(new NeuralNetwork());

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(spiedNeuralNetwork));

    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(requiredNumberOfMessagesToMakeChoice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(3))
      .isHuman();

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(3))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .incrementTests_failed();

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .save(ArgumentMatchers.eq(spiedNeuralNetwork));
  }

  @Test
  public void shouldSendYouAreRightEventBecauseChoiceEqualsItsAHumanAndIsHumanMethodReturnsTrue()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_HUMAN)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Human spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(requiredNumberOfMessagesToMakeChoice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(3))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(2))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_RIGHT)));

    Mockito.verify(neuralNetworkRepository, Mockito.times(0))
      .save(ArgumentMatchers.any(NeuralNetwork.class));
  }

  @Test
  public void shouldSendYouAreWrongEventBecauseChoiceEqualsItsAHumanAndIsHumanMethodReturnsFalse()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_HUMAN)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    NeuralNetwork spiedNeuralNetwork = Mockito.spy(new NeuralNetwork());

    Interlocutor spiedInterlocutor = Mockito.spy(new Machine(spiedNeuralNetwork));

    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(requiredNumberOfMessagesToMakeChoice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(3))
      .isHuman();

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(3))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .incrementTests_passed();

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .save(ArgumentMatchers.eq(spiedNeuralNetwork));
  }

  @Test
  public void shouldSendYouAreWrongEventBecauseChoiceEqualsItsAMachineAndIsHumanMethodReturnsTrue()
    throws IOException {
    MakeChoiceEvent spiedMakeChoiceEvent = Mockito.spy(new MakeChoiceEvent());
    Mockito.doReturn(Choice.ITS_A_MACHINE)
      .when(spiedMakeChoiceEvent)
      .getChoice();

    Human spiedInterlocutor = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedInterlocutor)
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Human spiedSender = Mockito.spy(new Human(null));
    Mockito.doNothing()
      .when(spiedSender)
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));

    Room spiedRoom = Mockito.spy(new Room(spiedSender, spiedInterlocutor));
    Mockito.doReturn(requiredNumberOfMessagesToMakeChoice)
      .when(spiedRoom)
      .getMessageCounter();
    Mockito.doReturn(spiedInterlocutor)
      .when(spiedRoom)
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doReturn(Optional.of(spiedRoom))
      .when(roomStorage)
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.doNothing()
      .when(roomService)
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    roomService.makeChoice(spiedSender, spiedMakeChoiceEvent);

    Mockito.verify(roomStorage, Mockito.times(1))
      .findByInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(roomService, Mockito.times(1))
      .excludeRoom(ArgumentMatchers.eq(spiedRoom));

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getMessageCounter();

    Mockito.verify(spiedRoom, Mockito.times(1))
      .getAnotherInterlocutor(ArgumentMatchers.eq(spiedSender));

    Mockito.verify(spiedInterlocutor, Mockito.times(3))
      .isHuman();

    Mockito.verify(spiedInterlocutor, Mockito.times(1))
      .send(ArgumentMatchers.any(DisconnectEvent.class));

    Mockito.verify(spiedMakeChoiceEvent, Mockito.times(3))
      .getChoice();

    Mockito.verify(spiedSender, Mockito.times(1))
      .send(ArgumentMatchers.eq(new Event(EventType.YOU_ARE_WRONG)));

    Mockito.verify(neuralNetworkRepository, Mockito.times(0))
      .save(ArgumentMatchers.any(NeuralNetwork.class));
  }
}
