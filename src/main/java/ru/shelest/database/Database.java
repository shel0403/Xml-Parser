package ru.shelest.database;

import ru.shelest.model.Person;

import java.util.List;

public interface Database {

    void insert(Person person);

    void insertDistinct(List<Person> persons);

    List<Person> getAll();

    Person getById(long id);

    void updateById(long id, Person person);

    void deleteById(long id);

    void closeConnection();
}
