package ru.dima.cli.console;

import java.util.Scanner;

public class ConsoleReader {
    public String read(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

}
