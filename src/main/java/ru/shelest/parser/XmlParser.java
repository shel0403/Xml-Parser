package ru.shelest.parser;

import org.xml.sax.SAXException;
import ru.shelest.model.Person;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface XmlParser {

    List<Person> parse() throws ParserConfigurationException, IOException, SAXException;
}
