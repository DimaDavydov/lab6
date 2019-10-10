package ru.dima.cli.console;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import ru.dima.server.model.UnnamedShorties;

@Setter
@Getter
public class Command {
    private TypeCommand typeCommand;
    private Argument argument;

    @Setter
    @Getter
    public static class Argument {
        private UnnamedShorties element;
        private File file;
        private String string;
    }

    public enum TypeCommand {
        INFO,
        SHOW,
        ADD,
        REMOVE_LOWER,
        ADD_IF_MIN,
        IMPORT,
        SAVE,
        LOAD,
        REMOVE,
        HELP,
        EXIT;

        private static Map<String, TypeCommand> map
            = Stream.of(TypeCommand.values()).collect(Collectors.toMap(Enum::name, type -> type));

        public static TypeCommand fromString(String type) {
            return map.get(type.toUpperCase());
        }
    }
}
