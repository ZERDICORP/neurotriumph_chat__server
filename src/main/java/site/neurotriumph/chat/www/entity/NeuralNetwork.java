package site.neurotriumph.chat.www.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "neural_network")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NeuralNetwork {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long owner_id;

  @Column(nullable = false)
  private String name;

  @Column(length = 2048, nullable = false)
  private String api_root;

  @Column(nullable = false)
  private String api_secret;

  @Column(columnDefinition = "TINYINT(1) DEFAULT 1", nullable = false)
  private boolean active = true;

  @Column(columnDefinition = "INT(11) DEFAULT 0", nullable = false)
  private int tests_passed;

  @Column(columnDefinition = "INT(11) DEFAULT 0", nullable = false)
  private int tests_failed;

  public NeuralNetwork(Long owner_id, String name, String api_root, String api_secret) {
    this.owner_id = owner_id;
    this.name = name;
    this.api_root = api_root;
    this.api_secret = api_secret;
  }
}
