package ru.dima.server.network;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data @AllArgsConstructor
@NoArgsConstructor @Wither
public class CollectionInfo {

  // Имя коллекции, используемой в данный момент
  private String collectionName;

  // Класс коллекции
  private String collectionType;

  // Дата инициализации
  private Date initializationDate;

  // Количество элементов в коллекции
  private Integer elementsAmount;
}
