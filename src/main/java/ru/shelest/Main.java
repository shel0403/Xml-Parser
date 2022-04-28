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
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Path xmlSource = Paths.get("/home/shel0403/tmp/persons.xml");
        XmlParser xmlParser = new DomXmlParser(xmlSource);

        Database database = new SqlDatabase();

        List<Person> existedPersons = database.getAll();
        List<Person> newPersons;

        try {
            newPersons = xmlParser.parse();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        existedPersons.addAll(newPersons);

        existedPersons.stream()
                .distinct()
                .forEach(System.out::println);
    }
}
