package ru.shelest;

import ru.shelest.database.Database;
import ru.shelest.database.SqlDatabase;
import ru.shelest.parser.DomXmlParser;
import ru.shelest.parser.XmlParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        Path xmlSource = Paths.get(args[0]);
        XmlParser xmlParser = new DomXmlParser(xmlSource);
        Database database = new SqlDatabase();

        try {
            database.insertDistinct(xmlParser.parse());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }
}
