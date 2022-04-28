package ru.shelest.database;

import ru.shelest.model.Person;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SqlDatabase implements Database {

    private static final String DATA_SOURCE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERNAME = "shel0403";
    private static final String PASSWORD = "qc6s8_c4N2";

    private final Connection connection;

    public SqlDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }

        try {
            this.connection = DriverManager.getConnection(DATA_SOURCE_URL, USERNAME, PASSWORD);
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void insert(Person person) {
        String insertQuery = "INSERT INTO persons (id, first_name, last_name, birth_date) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement statement = connection.prepareStatement(insertQuery);

            statement.setLong(1, person.getId());
            statement.setString(2, person.getFirstName());
            statement.setString(3, person.getLastName());
            statement.setDate(4, Date.valueOf(person.getBirthDate()));

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertDistinct(List<Person> persons) {

    }

    @Override
    public List<Person> getAll() {
        List<Person> result = new ArrayList<>();

        try {
            Statement statement = this.connection.createStatement();
            String query = "SELECT * FROM persons ";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                LocalDate birthDate = resultSet
                        .getDate("birth_date")
                        .toLocalDate();

                result.add(new Person(id, firstName, lastName, birthDate));
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return result;
    }

    @Override
    public Person getById(long id) {
        Person result = null;
        String query = "SELECT * FROM persons WHERE id=?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                LocalDate birthDate = resultSet
                        .getDate("birth_date")
                        .toLocalDate();

                result = new Person(id, firstName, lastName, birthDate);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public void updateById(long id, Person person) {

    }

    @Override
    public void deleteById(long id) {

    }
}
