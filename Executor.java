package ru.dima.server;

import java.util.List;
import ru.dima.server.model.UnnamedShorties;
import ru.dima.server.network.CollectionInfo;

interface Executor {

  boolean remove(UnnamedShorties unnamedShorties);

  boolean loadCollection();

  boolean saveCollection();

  boolean addIfMin(UnnamedShorties unnamedShorties);

  int removeLower(UnnamedShorties unnamedShorties);

  boolean add(UnnamedShorties unnamedShorties);

  List<UnnamedShorties> getAllShorties();

  CollectionInfo getCollectionInfo();
}