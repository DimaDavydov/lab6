package ru.dima.cli.console;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import ru.dima.cli.parser.ParserCommand;
import ru.dima.client.Client;
import ru.dima.server.model.UnnamedShorties;
import ru.dima.server.network.CollectionInfo;
import ru.dima.server.network.ErrorType;
import ru.dima.server.network.RequestResult;

public class CommandHandler {

  private Client client;

  public CommandHandler(Client client) {
    Objects.requireNonNull(client);
    this.client = client;
  }

  public void commandHandler(String command) {
    Objects.requireNonNull(command);

    if (command.trim().isEmpty()) {
      return;
    }

    ParserCommand parser = new ParserCommand();
    Command parsedCommand;
    try {
      parsedCommand = parser.parseCommand(command);
    } catch (IOException e) {
      System.err.println("Ошибка при обработке команды");
      return;
    }

    if (parsedCommand.getTypeCommand() == null) {
      System.err.println("Команды " + command.split(" ")[0] + " не существует");
      return;
    }

    switch (parsedCommand.getTypeCommand()) {
      case INFO:
        RequestResult<CollectionInfo> infoResult = client.getInfo();
        if (infoResult.getSuccess()) {
          CollectionInfo result = infoResult.getCollectionInfo();
          System.out.println(
              "Имя текущей коллекции: " + result.getCollectionName()
                  + "\nТип текущей коллекции: " + result.getCollectionType()
                  + "\nКоличество элементов в коллекции: " + result.getElementsAmount()
                  + "\nДата инициализации коллекции: " + result.getInitializationDate()
          );
        } else {
          System.err
              .println("Не удалось выполнить команду. Ошибка: " + infoResult.getError().toString());
        }
        break;
      case SHOW:
        RequestResult<List<UnnamedShorties>> getAllShortiesResult = client.getAllShorties();
        if (getAllShortiesResult.getSuccess()) {
          List<UnnamedShorties> shortiesList = getAllShortiesResult.getShortiesList();
          if (shortiesList.isEmpty()) {
            System.out.println("Коллекция пустая :(");
          } else {
            System.out.println("Получено " + shortiesList.size() + " элементов:");
            shortiesList.forEach(System.out::println);
          }
        } else {
          System.err
              .println("Не удалось выполнить команду. Ошибка: " + getAllShortiesResult.getError()
                                                                                      .toString());
        }
        break;
      case ADD:
        if (parsedCommand.getArgument() == null) {
          System.err.println("Необходимо передать элемент!");
          break;
        }
        if (parsedCommand.getArgument().getElement() == null) {
          System.err.println("Ошибка при парсинге аргумента");
          break;
        }
        RequestResult<Boolean> addResult = client.add(parsedCommand.getArgument().getElement());
        if (addResult.getSuccess()) {
          if (addResult.getResult()) {
            System.out.println("Элемент успешно добавлен в коллекцию");
          } else {
            System.out.println("Элемент не был добавлен в коллекцию");
          }
        } else {
          System.err
              .println("Не удалось выполнить команду. Ошибка: " + addResult.getError().toString());
        }
        break;
      case REMOVE_LOWER:
        if (parsedCommand.getArgument() == null) {
          System.err.println("Необходимо передать элемент!");
          break;
        }
        if (parsedCommand.getArgument().getElement() == null) {
          System.err.println("Ошибка при парсинге аргумента");
          break;
        }
        RequestResult<Integer> removeLowerResult
            = client.removeLower(parsedCommand.getArgument().getElement());
        if (removeLowerResult.getSuccess()) {
          System.out
              .println(removeLowerResult.getResult() + " элементов было удалено из коллекции");
        } else {
          System.err
              .println("Не удалось выполнить команду. Ошибка: " + removeLowerResult.getError()
                                                                                   .toString());
        }
        break;
      case ADD_IF_MIN:
        if (parsedCommand.getArgument() == null) {
          System.err.println("Необходимо передать элемент!");
          break;
        }
        if (parsedCommand.getArgument().getElement() == null) {
          System.err.println("Ошибка при парсинге аргумента");
          break;
        }
        RequestResult<Boolean> addIfMinResult = client
            .addIfMin(parsedCommand.getArgument().getElement());
        if (addIfMinResult.getSuccess()) {
          if (addIfMinResult.getResult()) {
            System.out.println("Элемент успешно добавлен в коллекцию");
          } else {
            System.out.println("Элемент не был добавлен в коллекцию");
          }
        } else {
          System.err
              .println(
                  "Не удалось выполнить команду. Ошибка: " + addIfMinResult.getError().toString());
        }
        break;
      case IMPORT:
        if (parsedCommand.getArgument() == null) {
          System.err.println("Необходимо передать файл!");
          break;
        }
        if (parsedCommand.getArgument().getFile() == null) {
          System.err.println("Необходимо передать файл!");
          break;
        }
        RequestResult<Boolean> importResult = client
            .importFile(parsedCommand.getArgument().getFile());
        if (importResult.getSuccess()) {
          if (importResult.getResult()) {
            System.out.println("Элементы из файла успешно были добавлены в коллекцию");
          } else {
            System.out.println(
                "По причине божественного вмешательства элементы из файла не были добавлены в коллекцию");
          }
        } else {
          System.err
              .println(
                  "Не удалось выполнить команду. Ошибка: " + importResult.getError().toString());
        }
        break;
      case SAVE:
        RequestResult<Boolean> saveResult = client.save();
        if (saveResult.getSuccess()) {
          if (saveResult.getResult()) {
            System.out.println("Коллекция была успешно сохранена");
          } else {
            System.out.println(
                "Что-то пошло не так и коллекция с именем "
                    + parsedCommand.getArgument().getString()
                    + " не сохранилась");
          }
        } else {
          System.err
              .println("Не удалось выполнить команду. Ошибка: " + saveResult.getError().toString());
        }
        break;
      case LOAD:
        RequestResult<Boolean> load = client.load();
        if(!load.getSuccess()) {
          if(load.getError() == ErrorType.COLLECTION_IS_NOT_IMPORTED) {
            System.err.println("Чтобы загрузить коллекцию на сервер необходимо её импортировать");
          } else {
            System.err.println("Произошла ошибка при загрузки импортированной коллекции");
          }
          break;
        }

        if(load.getResult()) {
          System.out.println("Импортированная коллекция была успешно загружена на сервер");
        } else {
          System.out.println("Импортировнная коллекция частично была загружена на сервер");
        }

        break;
      /*case LOAD:
        if (parsedCommand.getArgument() == null) {
          if(client.checkConnection().getSuccess()) {
            System.out.println("Коллекция была успешно загружена на сервер");
          } else {
            System.err.println("Не получилось загрузить коллекцию на сервер");
          }
          break;
        }
        if (parsedCommand.getArgument().getString() == null || parsedCommand.getArgument()
                                                                            .getString().trim()
                                                                            .isEmpty()) {
          System.err.println("Необходимо передать название коллекции!");
          break;
        }
        RequestResult<Boolean> loadResult = client.load(parsedCommand.getArgument().getString());
        if (loadResult.getSuccess()) {
          if (loadResult.getResult()) {
            if (parsedCommand.getArgument().getString() != null) {
              System.out.println(
                  "Коллекция " + parsedCommand.getArgument().getString()
                      + " была успешно загружена");
            } else {
              System.out.println(
                  "Коллекция была успешно загружена");
            }
          } else {
            System.err.println(
                "Что-то пошло не так и коллекция с именем "
                    + parsedCommand.getArgument().getString()
                    + " не загрузилась"
            );
          }
        } else {
          System.err
              .println("Не удалось выполнить команду. Ошибка: " + loadResult.getError().toString());
        }
        break;*/
      case REMOVE:
        if (parsedCommand.getArgument() == null) {
          System.err.println("Необходимо передать элемент!'");
          break;
        }
        if (parsedCommand.getArgument().getElement() == null) {
          System.err.println("Ошибка при парсинге аргумента");
          break;
        }
        RequestResult<Boolean> removeResult = client
            .remove(parsedCommand.getArgument().getElement());
        if (removeResult.getSuccess()) {
          if (removeResult.getResult()) {
            System.out.println("Элемент был успешно удален из коллекции");
          } else {
            System.out.println("Элемент не был удален из коллекции");
          }
        } else {
          System.err
              .println(
                  "Не удалось выполнить команду. Ошибка: " + removeResult.getError().toString());
        }
        break;
      case HELP:
        printHelp();
        break;
      case EXIT:
        System.out.println("Работа приложения завершается. Хорошего дня!");
        System.exit(0);
        break;
    }
  }

  private void printHelp() {
    System.out.println(
        "Список доступных команд:\n" +
            "info - показать информацию о коллекции\n" +
            "show - показать все элементы коллекции\n" +
            "add {json} - добавить новый элемент в коллекцию\n" +
            "remove_lower {json} - удалить из коллекции все элементы, меньшие, чем заданный\n" +
            "add_if_min {json} - добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции\n"
            +
            "import {file} - добавить в коллекцию все данные из файла\n" +
            "remove {json} - удалить элемент из коллекции\n" +
            "save - сохранить коллекцию под указанным именем\n" +
            "load {name} - загрузить коллекцию с указанным именем\n" +
            "help - показать список доступных команд\n" +
            "exit - выйти из программы"
    );
  }
}
