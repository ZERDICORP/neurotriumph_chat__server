package site.neurotriumph.chat.www.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServer implements Runnable {
  private Logger logger;
  private final int PORT;
  private boolean isRunning;
  private ServerSocket serverSocket;

  {
    logger = LoggerFactory.getLogger(EchoServer.class);
    PORT = 8008;
  }

  @Override
  public void run() {
    try {
      isRunning = true;
      serverSocket = new ServerSocket(PORT);

      logger.info("EchoServer has been started on port " + PORT + "..");

      while (isRunning) {
        Socket clientSocket = serverSocket.accept();

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("Hello, world!");
      }
    } catch (IOException e) {
      if (Objects.equals(e.getMessage(), "Socket closed")) {
        logger.info("EchoServer has been stopped..");
        return;
      }

      throw new RuntimeException(e);
    }
  }

  public void stop() throws IOException {
    isRunning = false;
    serverSocket.close();
  }
}