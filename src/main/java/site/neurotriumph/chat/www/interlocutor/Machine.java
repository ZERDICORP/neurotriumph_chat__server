package site.neurotriumph.chat.www.interlocutor;

import java.util.Collections;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;
import site.neurotriumph.chat.www.pojo.Event;

public class Machine extends Interlocutor {
  private final RestTemplate restTemplate;
  private final HttpHeaders headers;
  @Getter
  private final NeuralNetwork neuralNetwork;
  private Runnable onError;
  @Getter
  private ChatMessageEvent response;

  {
    restTemplate = new RestTemplate();
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
  }

  public Machine(NeuralNetwork neuralNetwork) {
    super(false);
    this.neuralNetwork = neuralNetwork;

    headers.add("API-Secret", neuralNetwork.getApi_secret());
  }

  public Machine onError(Runnable onError) {
    this.onError = onError;
    return this;
  }

  @Override
  public void send(Event event) {
    try {
      final HttpEntity<Event> entity = new HttpEntity<>(event, headers);
      response = restTemplate.postForObject(neuralNetwork.getApi_root() + "/reply", entity,
        ChatMessageEvent.class);
    } catch (Exception e) {
      response = null;
      onError.run();
    }
  }
}
