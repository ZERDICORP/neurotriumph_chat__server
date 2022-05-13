package site.neurotriumph.chat.www.util;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpiedScheduledFuture implements ScheduledFuture<Void> {
  @Override
  public long getDelay(TimeUnit timeUnit) {
    return 0;
  }

  @Override
  public int compareTo(Delayed delayed) {
    return 0;
  }

  @Override
  public boolean cancel(boolean b) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public Void get() throws InterruptedException, ExecutionException {
    return null;
  }

  @Override
  public Void get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
    return null;
  }
}
