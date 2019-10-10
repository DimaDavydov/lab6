package ru.dima.server;

import java.net.SocketException;

public interface Server {

  void start(int port) throws SocketException;

  void stop();
}
