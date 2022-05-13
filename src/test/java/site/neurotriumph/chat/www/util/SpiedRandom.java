package site.neurotriumph.chat.www.util;

import java.util.Random;

public class SpiedRandom extends Random {
  public SpiedRandom() {
    super();
  }

  @Override
  public int nextInt(int bound) {
    return super.nextInt(bound);
  }
}