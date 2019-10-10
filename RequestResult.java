package ru.dima.server.network;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import ru.dima.server.model.UnnamedShorties;

@Data @AllArgsConstructor
@NoArgsConstructor @Wither
public class RequestResult<T> {

  private static final ObjectMapper mapper = new ObjectMapper().registerModules();

  private Boolean success;

  private ErrorType error;
  private Exception exception;

  private T result;

  @JsonIgnore
  public List<UnnamedShorties> getShortiesList() {
    return mapper.convertValue(result, new TypeReference<List<UnnamedShorties>>(){});
  }

  @JsonIgnore
  public CollectionInfo getCollectionInfo() {
    return mapper.convertValue(result, new TypeReference<CollectionInfo>(){});
  }

  public static <T> RequestResult<T> createSuccessResultWith(T result) {
    return new RequestResult<T>()
        .withSuccess(true)
        .withResult(result);
  }

  public static <T> RequestResult<T> createSuccessResult() {
    return new RequestResult<T>()
        .withSuccess(true);
  }

  public static <T> RequestResult<T> createFailResultWith(ErrorType error, Exception exception) {
    return new RequestResult<T>()
        .withSuccess(false)
        .withError(error)
        .withException(exception);
  }

  public static <T> RequestResult<T> createFailResultWith(ErrorType error) {
    return new RequestResult<T>()
        .withSuccess(false)
        .withError(error);
  }
}
