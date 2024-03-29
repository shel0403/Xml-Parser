package ru.shelest.database;

import ru.shelest.model.Person;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SqlDatabase implements Database {

    private static final String DATA_SOURCE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties properties = new Properties();
        Path propertiesSource = Paths.get("/home/shel0403/tmp/postgres_creds.properties");

        try (InputStream inputStream = new FileInputStream(propertiesSource.toFile())) {
            properties.load(inputStream);

            USERNAME = properties.getProperty("username");
            PASSWORD = properties.getProperty("password");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

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

        createIfNotExists();
    }

    private void createIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS persons (\n" +
                "    id BIGINT NOT NULL,\n" +
                "    first_name VARCHAR,\n" +
                "    last_name VARCHAR,\n" +
                "    birth_date DATE\n" +
                ") ";

        try (Statement statement = this.connection.createStatement()) {
            this.connection.setAutoCommit(false);

            statement.executeUpdate(query);

            this.connection.commit();
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void insert(Person person) {
        String insertQuery = "INSERT INTO persons (id, first_name, last_name, birth_date) " +
                "VALUES (?, ?, ?, ?) ";

        try (PreparedStatement statement = this.connection.prepareStatement(insertQuery)) {
            this.connection.setAutoCommit(false);

            statement.setLong(1, person.getId());
            statement.setString(2, person.getFirstName());
            statement.setString(3, person.getLastName());
            statement.setDate(4, Date.valueOf(person.getBirthDate()));

            statement.executeUpdate();

            this.connection.commit();
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void insertDistinct(List<Person> persons) {
        List<Person> existed = this.getAll();
        List<Person> copy = new ArrayList<>(persons);
        copy.removeAll(existed);
        List<Person> toInsert = copy.stream()
                .distinct()
                .collect(Collectors.toList());

        String query = "INSERT INTO persons (id, first_name, last_name, birth_date) " +
                "VALUES (?, ?, ?, ?) ";

        try (PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            this.connection.setAutoCommit(false);

            for (Person person : toInsert) {
                statement.setLong(1, person.getId());
                statement.setString(2, person.getFirstName());
                statement.setString(3, person.getLastName());
                statement.setDate(4, Date.valueOf(person.getBirthDate()));

                statement.addBatch();
            }

            statement.executeBatch();

            this.connection.commit();
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public List<Person> getAll() {
        List<Person> result = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            this.connection.setAutoCommit(false);

            String query = "SELECT * FROM persons ";
            ResultSet resultSet = statement.executeQuery(query);

            this.connection.commit();

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
        String query = "SELECT * FROM persons WHERE id=? ";

        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            this.connection.setAutoCommit(false);

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            this.connection.commit();

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
        Person personToUpdate = this.getById(id);

        if (personToUpdate != null) {
            String query = "UPDATE persons " +
                    "SET " +
                    "first_name = ?, " +
                    "last_name = ?, " +
                    "birth_date = ? " +
                    "WHERE id = ? ";

            try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                this.connection.setAutoCommit(false);

                statement.setString(1, person.getFirstName());
                statement.setString(2, personToUpdate.getLastName());
                statement.setDate(3, Date.valueOf(person.getBirthDate()));
                statement.setLong(4, id);

                statement.executeUpdate();

                this.connection.commit();
            } catch (SQLException e) {
                try {
                    this.connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public void deleteById(long id) {
        String query = "DELETE FROM persons " +
                "WHERE id = ? ";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            this.connection.setAutoCommit(false);

            statement.setLong(1, id);

            statement.executeUpdate();

            this.connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
