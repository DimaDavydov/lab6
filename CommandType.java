package ru.dima.server.model;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CommandType {
  CHECK_CONNECTION,
  INFO,
  GET_ALL_SHORTIES,
  ADD,
  REMOVE_LOWER,
  ADD_IF_MIN,
  SAVE,
  LOAD,
  REMOVE;

  private static Map<String, CommandType> commandTypesMap
      = Stream.of(CommandType.values()).collect(Collectors.toMap(Enum::name, type -> type));

  public static CommandType fromString(String type) {
    return commandTypesMap.get(type.toUpperCase());
  }
}
