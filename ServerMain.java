package ru.dima.server;

import java.io.IOException;
import java.net.SocketException;

public class ServerMain {

  public static void main(String[] args) throws SocketException {
    Server server = null;
    try {
      server = new ServerImpl();
    } catch (IOException e) {
      System.err.println("При создании сервера произошла ошибка!");
    }

    server.start(1478);
  }
}
