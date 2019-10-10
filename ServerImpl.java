package ru.dima.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import ru.dima.server.model.CommandType;
import ru.dima.server.model.UnnamedShorties;
import ru.dima.server.network.ErrorType;
import ru.dima.server.network.Request;
import ru.dima.server.network.RequestResult;

public class ServerImpl implements Server {

  private ObjectMapper mapper;

  private DatagramSocket socket;
  private Map<String, ServerRunnable> activeRunnables;

  // collection name -> executor
//  private Map<String, Executor> executors = new ConcurrentHashMap<>();
  private AtomicReference<ServerExecutor> executorReference;


  public ServerImpl() throws IOException {
    mapper = new ObjectMapper().registerModules();
    activeRunnables = new ConcurrentHashMap<>();

    executorReference = new AtomicReference<>(new ServerExecutor(ServerExecutor.getDefaultName()));
  }

  @Override public void start(int port) throws SocketException {
    if (socket != null && socket.isConnected()) {
      throw new IllegalStateException("Server is already running");
    }

    if (socket != null && !socket.isConnected() && !socket.isClosed()) {
      socket.close();
    }

    socket = new DatagramSocket(port);

    new Thread(this::serverRoutine).start();
  }

  @Override public void stop() {
    if (socket == null) {
      throw new IllegalStateException("Server is not running yet");
    }

    if (socket.isClosed()) {
      return;
    }

    if (!socket.isConnected()) {
      socket.close();
      return;
    }

    while (socket.isConnected()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    socket.close();
  }

  private void serverRoutine() {
    try {
      while (!socket.isClosed()) {
        byte[] buffer = new byte[1048576];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        socket.receive(packet);

        Request request = mapRequest(packet);

        String identifier;
        if (request.getIdentifier() == null) {
          identifier = String.valueOf((int) (Math.random() * Integer.MAX_VALUE));
        } else {
          identifier = request.getIdentifier();
        }

        ServerRunnable runnable = activeRunnables.get(identifier);
        if (runnable == null) {
          runnable = new ServerRunnable(identifier);
          activeRunnables.put(identifier, runnable);

          Thread thread = new Thread(runnable);
          thread.start();
        }

        InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
        runnable.addTask(new ServerTask()
            .withAddress(address)
            .withRequest(request)
        );
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private <T> boolean sendResponse(InetSocketAddress address, RequestResult<T> result) {
    try {
      String json = mapper.writeValueAsString(result);
      byte[] bytes = json.getBytes();

      DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
      packet.setSocketAddress(address);

      socket.send(packet);

      return true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private Request mapRequest(DatagramPacket packet) {
    byte[] src = packet.getData();
    int length = packet.getLength();

    byte[] data = new byte[length];
    System.arraycopy(src, 0, data, 0, length);

    String json = new String(data);
    try {
      return mapper.readValue(json, Request.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private class ServerRunnable implements Runnable {

    private String clientIdentifier;
    private AtomicReference<ServerExecutor> executor;
    private Queue<ServerTask> tasksQueue = new ConcurrentLinkedQueue<>();


    public ServerRunnable(String clientIdentifier) {
      this.clientIdentifier = Objects.requireNonNull(clientIdentifier);

      this.executor = executorReference;
    }

    @Override public void run() {
      while (true) {
        ServerTask task = tasksQueue.poll();
        if (task == null) {
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          continue;
        }

        handleTask(task);
      }
    }

    public void handleTask(ServerTask task) {
      String signature = task.getRequest().getSignature();
      CommandType command;
      String[] args;
      List<UnnamedShorties> humansArg = task.getRequest().getShorties();

      int idx = signature.indexOf(' ');
      if (idx == -1) {
        command = CommandType.fromString(signature);
        args = new String[0];
      } else {
        command = CommandType.fromString(signature.substring(0, idx));
        args = signature.substring(idx + 1).split(" ");
      }

      Consumer<RequestResult<?>> response = requestResult -> {
        sendResponse(task.getAddress(), requestResult);
      };

      if (command == null) {
        response.accept(RequestResult.createFailResultWith(ErrorType.WRONG_SIGNATURE));
        return;
      }

      switch (command) {
        case SAVE:
          response.accept(RequestResult.createSuccessResultWith(executor.get().saveCollection()));
          break;
        /*case LOAD:
          if (args.length == 0) {
            response.accept(RequestResult.createFailResultWith(ErrorType.WRONG_SIGNATURE));
            return;
          }

          try {
            ServerExecutor newExecutor = new ServerExecutor(args[0]);
            this.executor.set(newExecutor);
            response.accept(RequestResult.createSuccessResultWith(true));
          } catch (Exception ex) {
            response.accept(RequestResult.createFailResultWith(ErrorType.IO_ERROR));
            return;
          }
          break;*/
        case CHECK_CONNECTION:
          response.accept(RequestResult.createSuccessResultWith(clientIdentifier));
          break;
        case INFO:
          response
              .accept(RequestResult.createSuccessResultWith(executor.get().getCollectionInfo()));
          break;
        case GET_ALL_SHORTIES:
          response.accept(RequestResult.createSuccessResultWith(executor.get().getAllShorties()));
          break;
        case ADD:
          if (humansArg == null) {
            response.accept(RequestResult.createFailResultWith(ErrorType.WRONG_REQUEST));
            return;
          } else if (humansArg.size() == 0) {
            response.accept(RequestResult.createSuccessResultWith(true));
            return;
          }
          boolean success = humansArg.stream()
                                     .map(human -> executor.get().add(human))
                                     .noneMatch(result -> false);

          response.accept(
              RequestResult.createSuccessResultWith(success)
          );

          break;
        case REMOVE:
          if (humansArg == null || humansArg.size() == 0) {
            response.accept(RequestResult.createFailResultWith(ErrorType.WRONG_REQUEST));
            return;
          }

          humansArg.forEach(human -> {
            response.accept(
                RequestResult.createSuccessResultWith(
                    executor.get().remove(human)
                )
            );
          });
          break;
        case REMOVE_LOWER:
          if (humansArg == null || humansArg.size() == 0) {
            response.accept(RequestResult.createFailResultWith(ErrorType.WRONG_REQUEST));
            return;
          }

          humansArg.forEach(human -> {
            response.accept(
                RequestResult.createSuccessResultWith(
                    executor.get().removeLower(human)
                )
            );
          });
          break;
        case ADD_IF_MIN:
          if (humansArg == null || humansArg.size() == 0) {
            response.accept(RequestResult.createFailResultWith(ErrorType.WRONG_REQUEST));
            return;
          }

          humansArg.forEach(human -> {
            response.accept(
                RequestResult.createSuccessResultWith(
                    executor.get().addIfMin(human)
                )
            );
          });
          break;
      }
    }

    public void addTask(ServerTask task) {
      tasksQueue.add(task);
    }
  }

  @Data @NoArgsConstructor
  @AllArgsConstructor @Wither
  private static class ServerTask {

    private InetSocketAddress address;
    private Request request;
  }
}
