package site.neurotriumph.chat.www;

import java.util.Optional;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
import site.neurotriumph.chat.www.interlocutor.Machine;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;
import site.neurotriumph.chat.www.service.LobbyService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FindMachineUnitTest {
  @Autowired
  private LobbyService lobbyService;
  @MockBean
  private NeuralNetworkRepository neuralNetworkRepository;

  @Test
  public void shouldNotFindNeuralNetworkAndReturnNull() {
    Mockito.doReturn(Optional.empty())
      .when(neuralNetworkRepository)
      .findOneRandom();

    Interlocutor interlocutor = lobbyService.findMachine();

    assertNull(interlocutor);

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();
  }

  @Test
  public void shouldFindNeuralNetworkAndReturnNewMachineAndRunOnErrorMethod() {
    NeuralNetwork spiedNeuralNetwork = Mockito.spy(new NeuralNetwork());

    Mockito.doReturn(Optional.of(spiedNeuralNetwork))
      .when(neuralNetworkRepository)
      .findOneRandom();

    Mockito.doReturn(spiedNeuralNetwork)
      .when(neuralNetworkRepository)
      .save(ArgumentMatchers.eq(spiedNeuralNetwork));

    Machine machine = Mockito.spy((Machine) lobbyService.findMachine());
    Runnable onError = (Runnable) ReflectionTestUtils.getField(machine, "onError");

    assertNotNull(onError);
    onError.run();

    assertNotNull(machine);

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .setInvalid_api(ArgumentMatchers.eq(true));

    Mockito.verify(spiedNeuralNetwork, Mockito.times(1))
      .setActive(ArgumentMatchers.eq(false));

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .save(ArgumentMatchers.eq(spiedNeuralNetwork));
  }
}
