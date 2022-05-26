package site.neurotriumph.chat.www.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.neurotriumph.chat.www.pojo.ChatMessageEvent;

public class EchoServer implements Runnable {
  private final Logger logger;
  private final int PORT;
  private final ObjectMapper objectMapper;
  private ServerSocket serverSocket;
  private volatile boolean isRunning;

  {
    logger = LoggerFactory.getLogger(EchoServer.class);
    PORT = 8008;
    objectMapper = new ObjectMapper();
  }

  @Override
  public void run() {
    try {
      isRunning = true;
      serverSocket = new ServerSocket(PORT);

      logger.info("EchoServer has been started on port " + PORT + "..");

      while (isRunning) {
        final Socket clientSocket = serverSocket.accept();

        logger.info("Connected: " + clientSocket.getInetAddress().getHostName());

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String body = objectMapper.writeValueAsString(new ChatMessageEvent("Hello, world!"));
        String response = "HTTP/1.1 200 OK\r\n" +
          "Content-Length: " + body.length() + "\r\n" +
          "Content-Type: application/json\r\n" +
          "\r\n" +
          body;

        logger.info("Response:\n" + response + "\n");

        out.println(response);
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

  public static void main(String[] args) {
    new Thread(new EchoServer()).start();
  }
}