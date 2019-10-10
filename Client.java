package ru.dima.client;

import java.io.File;
import java.util.List;
import ru.dima.server.model.UnnamedShorties;
import ru.dima.server.network.CollectionInfo;
import ru.dima.server.network.RequestResult;

public interface Client {

  RequestResult<String> connect(String host, int port, int clientPort);

  RequestResult<String> checkConnection();

  RequestResult<Boolean> importFile(File file);

  RequestResult<Boolean> add(UnnamedShorties shorties);

  RequestResult<List<UnnamedShorties>> getAllShorties();

  RequestResult<Boolean> remove(UnnamedShorties shorties);

  RequestResult<Boolean> addIfMin(UnnamedShorties shorties);

  RequestResult<Integer> removeLower(UnnamedShorties shorties);

  RequestResult<CollectionInfo> getInfo();

  RequestResult<Boolean> save();

  RequestResult<Boolean> load();
}
