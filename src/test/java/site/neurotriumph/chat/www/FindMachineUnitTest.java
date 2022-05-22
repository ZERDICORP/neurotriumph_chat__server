package site.neurotriumph.chat.www;

import java.util.Optional;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.interlocutor.Interlocutor;
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
  public void shouldFindNeuralNetworkAndReturnNewMachine() {
    Mockito.doReturn(Optional.of(new NeuralNetwork()))
      .when(neuralNetworkRepository)
      .findOneRandom();

    Interlocutor interlocutor = lobbyService.findMachine();

    assertNotNull(interlocutor);

    Mockito.verify(neuralNetworkRepository, Mockito.times(1))
      .findOneRandom();
  }
}
