package ru.shelest;

import org.xml.sax.SAXException;
import ru.shelest.database.Database;
import ru.shelest.database.SqlDatabase;
import ru.shelest.model.Person;
import ru.shelest.parser.DomXmlParser;
import ru.shelest.parser.XmlParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
