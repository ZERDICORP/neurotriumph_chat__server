package site.neurotriumph.chat.www.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.neurotriumph.chat.www.entity.NeuralNetwork;

@Repository
public interface NeuralNetworkRepository extends JpaRepository<NeuralNetwork, Long> {
  @Query(value = "SELECT * FROM neural_network ORDER BY RAND() LIMIT 1", nativeQuery = true)
  Optional<NeuralNetwork> findOneRandom();
}