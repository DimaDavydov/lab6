package ru.dima.cli;

import ru.dima.cli.console.CommandHandler;
import ru.dima.cli.console.ConsoleReader;
import ru.dima.client.Client;
import ru.dima.client.ClientImpl;
import ru.dima.server.network.ErrorType;
import ru.dima.server.network.RequestResult;

public class CliMain {

  private static final String SERVER_HOST = "localhost";
  private static final Integer SERVER_PORT = 1478;

  private static final Integer HANDSHAKE_TRIES = 10;

  public static void main(String[] args) {
    Client client = new ClientImpl();

    System.out.println("Установка соединение с сервером...");
    RequestResult<String> connectResult;

    Integer clientPort = 10_000 + ((int) (Math.random() * 50_000));

    for (int i = 1; i <= HANDSHAKE_TRIES; ++i) {
      connectResult = client.connect(SERVER_HOST, SERVER_PORT, clientPort);
      if (connectResult.getSuccess()) {
        System.out.println("Соединение с сервером успешно установлено! Идентификатор: " + connectResult.getResult());
        break;
      } else if (connectResult.getError() == ErrorType.TIMEOUT_ERROR) {
        System.err.println("Попытка [" + i + "/" + HANDSHAKE_TRIES + "]: Сервер недоступен. Попробуйте подождать");
        if(i == HANDSHAKE_TRIES) {
          System.exit(-1);
        }
      } else {
        System.err.println("Произошла ошибка при подключении к серверу!");
        System.exit(-1);
      }
    }

    CommandHandler handler = new CommandHandler(client);
    ConsoleReader reader = new ConsoleReader();

    System.out.println("Начните вводить команды. Для получения справки введите 'help'");
    while (true) {
      try {
        handler.commandHandler(reader.read());
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
