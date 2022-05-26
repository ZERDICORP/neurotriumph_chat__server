package site.neurotriumph.chat.www.util;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component("spiedRandom")
public class SpiedRandom extends Random {
  public SpiedRandom() {
    super();
  }

  @Override
  public int nextInt(int bound) {
    return super.nextInt(bound);
  }
}