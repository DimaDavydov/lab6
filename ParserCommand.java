package ru.dima.cli.parser;

import java.io.File;
import java.io.IOException;
import ru.dima.server.ParserJson;
import ru.dima.cli.console.Command;
import ru.dima.cli.console.Command.TypeCommand;
import ru.dima.server.model.AgeException;
import ru.dima.server.model.UnnamedShorties;

public class ParserCommand {
    private ParserJson parserJson = new ParserJson();

    public Command parseCommand(String crudeCommand) throws IOException {
        Command command = new Command();
        if (crudeCommand.contains(" ")) {
            int idx = crudeCommand.indexOf(' ');
            String type = crudeCommand.substring(0, idx);
            String crudeArgument = crudeCommand.substring(idx + 1);;

            TypeCommand typeCommand = TypeCommand.fromString(type);
            command.setTypeCommand(typeCommand);


            Command.Argument argument = new Command.Argument();
            if (command.getTypeCommand() == TypeCommand.IMPORT) {
                argument.setFile(new File(crudeArgument));
            } /*else if (command.getTypeCommand() == TypeCommand.LOAD) {
                argument.setString(crudeArgument);
            }*/ else {
                UnnamedShorties shorties = parserJson.fromJson(crudeArgument);

                try {
                    if (shorties.getShortyName() == null || shorties.getShortyPosition() == null
                        || shorties.getWeight() <= 0 || shorties.getAge() <= 0) {
                        argument.setElement(null);
                    } else {
                        argument.setElement(shorties);
                    }
                } catch (AgeException e) {
                    argument.setElement(null);
                }
            }

            command.setArgument(argument);
        } else {
            command.setTypeCommand(Command.TypeCommand.fromString(crudeCommand.toUpperCase()));
        }
        return command;
    }
}
