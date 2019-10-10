package ru.dima.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import ru.dima.server.model.UnnamedShorties;
import ru.dima.server.network.CollectionInfo;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ServerExecutor implements Executor {

  private static final File DIRECTORY = new File("~/");

  @Getter
  private static final String defaultName = "DefaultName";

  private final String collectionName;

  private Set<UnnamedShorties> store = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private Date date = new Date();

  static {
    if (!DIRECTORY.exists()) {
      DIRECTORY.mkdirs();
    }
  }

  {
    Runtime.getRuntime().addShutdownHook(new Thread(this::saveCollection));
  }

  public ServerExecutor(String collectionName) throws IOException {
    File file = new File(DIRECTORY, collectionName);

    if (file.exists()) {
      ObjectMapper objectMapper = new ObjectMapper();
      Set<UnnamedShorties> set = objectMapper.readValue(
          readFile(file), new TypeReference<HashSet<UnnamedShorties>>() {
          }
      );
      store.addAll(set);
    }

    this.collectionName = collectionName;
  }

  public boolean remove(UnnamedShorties unnamedShorties) {
    return store.remove(unnamedShorties);
  }

  @Override public boolean loadCollection() {
    throw new NotImplementedException();
  }

  public boolean saveCollection() {
    File file = new File(DIRECTORY, collectionName);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        return false;
      }
    }

    ObjectMapper objectMapper = new ObjectMapper();
    try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
        new FileOutputStream(file))) {
      objectMapper.writeValue(outputStreamWriter, store);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public boolean addIfMin(UnnamedShorties unnamedShorties) {
    if (unnamedShorties.getShortyName() == null
        || unnamedShorties.getShortyPosition() == null) {
      return false;
    }

    UnnamedShorties minElement = store.stream().sorted().findFirst()
                                      .orElseThrow(NullPointerException::new);
    if (unnamedShorties.compareTo(minElement) < 0) {
      return store.add(unnamedShorties);
    } else {
      return false;
    }
  }

  public int removeLower(UnnamedShorties unnamedShorties) {
    int oldSize = store.size();
    store.removeIf((UnnamedShorties element) -> unnamedShorties.compareTo(element) > 0);
    int newSize = store.size();
    return (oldSize - newSize);
  }

  public boolean add(UnnamedShorties unnamedShorties) {
    if (unnamedShorties.getShortyName() == null
        || unnamedShorties.getShortyPosition() == null) {
      return false;
    }

    return store.add(unnamedShorties);
  }

  public List<UnnamedShorties> getAllShorties() {
    return new ArrayList<>(store);
  }

  public CollectionInfo getCollectionInfo() {
    return new CollectionInfo()
        .withCollectionName(this.collectionName)
        .withCollectionType(store.getClass().getSimpleName())
        .withInitializationDate(date)
        .withElementsAmount(store.size());
  }

  private static String readFile(File file) throws FileNotFoundException {
    Scanner scanner = new Scanner(file);
    StringBuilder result = new StringBuilder();
    while (scanner.hasNextLine()) {
      result.append(scanner.nextLine());
    }
    return result.toString();
  }
}
