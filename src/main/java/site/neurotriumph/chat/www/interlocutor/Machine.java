package site.neurotriumph.chat.www.interlocutor;

import org.springframework.web.socket.TextMessage;
import site.neurotriumph.chat.www.entity.NeuralNetwork;

public class Machine extends Interlocutor {
  private final NeuralNetwork neuralNetwork;

  public Machine(NeuralNetwork neuralNetwork) {
    super(false);
    this.neuralNetwork = neuralNetwork;
  }

  @Override
  public void send(TextMessage message) {
    // TODO: send http request to neural network api

    System.out.println("Message sent to neural network: " + message);
  }
}
