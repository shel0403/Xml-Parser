package ru.shelest.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.shelest.model.Person;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.shelest.utils.ConditionChecker.require;

public class DomXmlParser implements XmlParser {

    private static final String INCORRECT_XML_EXCEPTION_MESSAGE = "Incorrect XML!";

    private final Path xmlSource;

    public DomXmlParser(Path xmlSource) {
        this.xmlSource = xmlSource;
    }

    @Override
    public List<Person> parse() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document = documentBuilderFactory.newDocumentBuilder().parse(xmlSource.toFile());

        Node rootNode = document.getFirstChild();
        NodeList childRootNodes = rootNode.getChildNodes();

        NodeList personNodes = IntStream.range(0, childRootNodes.getLength())
                .mapToObj(childRootNodes::item)
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .filter(node -> "persons".equals(node.getNodeName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(INCORRECT_XML_EXCEPTION_MESSAGE))
                .getChildNodes();

        List<Node> personList = IntStream.range(0, personNodes.getLength())
                .mapToObj(personNodes::item)
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .filter(node -> "person".equals(node.getNodeName()))
                .collect(Collectors.toList());

        return personList.stream()
                .map(this::nodeToPerson)
                .collect(Collectors.toList());
    }

    private Person nodeToPerson(Node personNode) {
        NodeList childNodes = personNode.getChildNodes();

        long id = -1;
        String firstName = null;
        String lastName = null;
        LocalDate birthDate = null;

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                String currentNodeContent = currentNode.getTextContent();

                switch (currentNode.getNodeName()) {
                    case "id":
                        id = Long.parseLong(currentNodeContent);
                        break;
                    case "firstname":
                        firstName = currentNodeContent;
                        break;
                    case "lastname":
                        lastName = currentNodeContent;
                        break;
                    case "birthdate":
                        birthDate = LocalDate.parse(currentNodeContent, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        break;
                    default:
                        throw new IllegalArgumentException(INCORRECT_XML_EXCEPTION_MESSAGE);
                }
            }
        }

        require(
                id != -1 && firstName != null && lastName != null && birthDate != null,
                () -> new IllegalArgumentException(INCORRECT_XML_EXCEPTION_MESSAGE)
        );

        return new Person(id, firstName, lastName, birthDate);
    }
}
