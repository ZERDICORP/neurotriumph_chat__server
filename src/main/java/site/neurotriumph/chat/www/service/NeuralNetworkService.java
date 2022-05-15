package site.neurotriumph.chat.www.service;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.neurotriumph.chat.www.entity.NeuralNetwork;
import site.neurotriumph.chat.www.repository.NeuralNetworkRepository;

@Service
public class NeuralNetworkService {
  @Autowired
  private NeuralNetworkRepository neuralNetworkRepository;

  @Transactional
  public void deactivate(Long id) {
    NeuralNetwork neuralNetwork = neuralNetworkRepository.findById(id)
      .orElseThrow(() -> new IllegalStateException("neural network not found.."));

    if (!neuralNetwork.isActive()) {
      neuralNetwork.setActive(false);
    }
  }
}
