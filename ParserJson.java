package ru.dima.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import ru.dima.server.model.UnnamedShorties;

public class ParserJson {

    public String toJson(UnnamedShorties unnamedShorties) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(unnamedShorties);
    }

    public UnnamedShorties fromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, UnnamedShorties.class);
    }
}
