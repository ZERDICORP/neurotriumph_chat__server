package site.neurotriumph.chat.www.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import site.neurotriumph.chat.www.pojo.Event;

public class EventQueue {
  private final int capacity;
  private final BlockingQueue<Event> blockingQueue;

  public EventQueue(int capacity) {
    this.capacity = capacity;
    this.blockingQueue = new ArrayBlockingQueue<>(capacity);
  }

  public void add(Event event) {
    blockingQueue.add(event);
  }

  public void waitUntilFull() throws InterruptedException {
    while (blockingQueue.size() < capacity) {
      Thread.sleep(100);
    }
  }

  public void waitUntil(int i) throws InterruptedException {
    while (blockingQueue.size() < i) {
      Thread.sleep(100);
    }
  }

  public Event poll() {
    return blockingQueue.poll();
  }

  public boolean isFull() {
    return blockingQueue.size() == capacity;
  }
}
