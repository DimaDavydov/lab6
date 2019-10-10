package ru.dima.server.network;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorType {
  SOCKET_CREATE_ERROR(0),
  JSON_PARSE_ERROR(1),
  CONNECTION_ERROR(2),
  PERMISSION_ERROR(3),
  IO_ERROR(4),
  WRONG_SIGNATURE(5),
  WRONG_REQUEST(6),
  TIMEOUT_ERROR(7),
  COLLECTION_IS_NOT_IMPORTED(8);

  private static Map<Integer, ErrorType> errorsMap
      = Stream.of(ErrorType.values())
              .collect(Collectors.toMap(ErrorType::getErrorCode, type -> type));

  private Integer errorCode;

  public static ErrorType fromErrorCode(Integer code) {
    return errorsMap.get(code);
  }
}
