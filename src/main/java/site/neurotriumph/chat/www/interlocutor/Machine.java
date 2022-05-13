package site.neurotriumph.chat.www.interlocutor;

import java.io.IOException;
import lombok.Getter;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.Event;

public class Machine extends Interlocutor {
  @Getter
  private ChatMessageEvent response;
  private final NeuralNetwork neuralNetwork;

  public Machine(NeuralNetwork neuralNetwork) {
    super(false);
    this.neuralNetwork = neuralNetwork;
  }

  @Override
  public void send(Event event) throws IOException {
    response = new ChatMessageEvent("Hello, world!"); // TODO: send http request to neural network api

    System.out.println("Message: " + objectMapper.writeValueAsString(event) + ", was sent to API: " +
      neuralNetwork.getApi_root());
  }
}
