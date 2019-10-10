package ru.dima.server.network;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import ru.dima.server.model.UnnamedShorties;

@Data @AllArgsConstructor
@NoArgsConstructor @Wither
public class Request {
  private String identifier;
  private String signature;
  private List<UnnamedShorties> shorties;
}
