package ru.dima.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ru.dima.server.model.CommandType;
import ru.dima.server.model.UnnamedShorties;
import ru.dima.server.network.CollectionInfo;
import ru.dima.server.network.ErrorType;
import ru.dima.server.network.Request;
import ru.dima.server.network.RequestResult;

public class ClientImpl implements Client {

  private String identifier;

  private DatagramSocket socket;
  private InetSocketAddress address;

  private ObjectMapper mapper = new ObjectMapper().registerModules();

  private List<UnnamedShorties> importedList;

  @Override
  public RequestResult<String> connect(String host, int port, int clientPort) {
    this.address = new InetSocketAddress(Objects.requireNonNull(host), port);

    try {
      socket = new DatagramSocket(clientPort);
    } catch (SocketException e) {
      return RequestResult.createFailResultWith(ErrorType.SOCKET_CREATE_ERROR, e);
    }
    RequestResult<String> result = checkConnection();
    if (!result.getSuccess()) {
      socket.close();
    } else {
      this.identifier = result.getResult();
    }

    return result;
  }

  public RequestResult<String> checkConnection() {
    // send packet to server
    Request request = new Request().withSignature(CommandType.CHECK_CONNECTION.toString());
    return sendRequest(request);
  }

  @Override public RequestResult<Boolean> importFile(File file) {
    Objects.requireNonNull(file);
    try {
      if (file.isDirectory() || !file.canRead()) {
        return RequestResult.createFailResultWith(ErrorType.IO_ERROR);
      }

      StringBuilder buffer = new StringBuilder();
      Files.lines(Paths.get(file.getPath())).forEach(buffer::append);
      String json = buffer.toString();

      ObjectMapper mapper = new ObjectMapper();

      try {
        List<UnnamedShorties> list = mapper.convertValue(
            mapper.readValue(json, List.class),
            new TypeReference<List<UnnamedShorties>>() {
            }
        );

        importedList = list;

        return RequestResult.createSuccessResultWith(true);
      } catch (Exception ex) {
        return RequestResult.createFailResultWith(ErrorType.IO_ERROR, ex);
      }
    } catch (IOException e) {
      return RequestResult.createFailResultWith(ErrorType.IO_ERROR, e);
    }
  }

  @Override public RequestResult<Boolean> add(UnnamedShorties shorties) {
    return sendRequest(new Request()
        .withSignature(CommandType.ADD.toString())
        .withShorties(Collections.singletonList(Objects.requireNonNull(shorties))));
  }

  @Override public RequestResult<List<UnnamedShorties>> getAllShorties() {
    return sendRequest(new Request().withSignature(CommandType.GET_ALL_SHORTIES.toString()));
  }

  @Override public RequestResult<Boolean> remove(UnnamedShorties shorties) {
    return sendRequest(
        new Request()
            .withSignature(CommandType.REMOVE.toString())
            .withShorties(Collections.singletonList(Objects.requireNonNull(shorties)))
    );
  }

  @Override public RequestResult<Boolean> addIfMin(UnnamedShorties shorties) {
    return sendRequest(
        new Request()
            .withSignature(CommandType.ADD_IF_MIN.toString())
            .withShorties(Collections.singletonList(Objects.requireNonNull(shorties)))
    );
  }

  @Override public RequestResult<Integer> removeLower(UnnamedShorties shorties) {
    return sendRequest(
        new Request()
            .withSignature(CommandType.REMOVE_LOWER.toString())
            .withShorties(Collections.singletonList(Objects.requireNonNull(shorties)))
    );
  }

  @Override public RequestResult<CollectionInfo> getInfo() {
    return sendRequest(new Request().withSignature(CommandType.INFO.toString()));
  }

  @Override public RequestResult<Boolean> save() {
    return sendRequest(
        new Request().withSignature(CommandType.SAVE.toString()));
  }

  @Override public RequestResult<Boolean> load() {
    if (importedList == null) {
      return RequestResult.createFailResultWith(ErrorType.COLLECTION_IS_NOT_IMPORTED);
    } else {
      return sendRequest(
          new Request()
              .withSignature(CommandType.ADD.toString())
              .withShorties(importedList)
      );
    }
  }

  private <T> RequestResult<T> sendRequest(Request request) {
    assert request != null;
    try {
      String json = mapper.writeValueAsString(request.withIdentifier(identifier));

      byte[] bytes = json.getBytes();

      DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
      packet.setSocketAddress(address);

      socket.send(packet);

      return receiveResponse();
    } catch (JsonProcessingException e) {
      return RequestResult.createFailResultWith(ErrorType.JSON_PARSE_ERROR, e);
    } catch (IOException e) {
      return RequestResult.createFailResultWith(ErrorType.IO_ERROR, e);
    }
  }

  private <T> RequestResult<T> receiveResponse() {
    byte[] buffer = new byte[1048576];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    CompletableFuture<RequestResult<T>> future = CompletableFuture.supplyAsync(
        () -> {
          try {
            socket.receive(packet);
            return mapResult(packet);
          } catch (IOException e) {
            return RequestResult.<T>createFailResultWith(ErrorType.CONNECTION_ERROR, e);
          }
        }
    );

    try {
      return future.get(10, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException e) {
      return RequestResult.createFailResultWith(ErrorType.CONNECTION_ERROR, e);
    } catch (TimeoutException e) {
      return RequestResult.createFailResultWith(ErrorType.TIMEOUT_ERROR, e);
    }
  }

  @SuppressWarnings("ALL")
  private <T> RequestResult<T> mapResult(DatagramPacket packet) {
    assert packet != null;
    byte[] src = packet.getData();
    int length = packet.getLength();

    byte[] data = new byte[length];
    System.arraycopy(src, 0, data, 0, length);

    String json = new String(data);
    try {
      return mapper.readValue(json, RequestResult.class);
    } catch (IOException e) {
      return RequestResult.createFailResultWith(ErrorType.JSON_PARSE_ERROR, e);
    }
  }
}
