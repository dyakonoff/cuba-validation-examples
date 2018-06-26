package com.company.passportnumber.service;

import com.company.passportnumber.entity.CountryCode;
import com.company.passportnumber.entity.Person;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.validation.CustomValidationException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

@Service(PersonApiService.NAME)
public class PersonApiServiceBean implements PersonApiService {

    @Inject
    private DataManager dataManager;

    @Inject
    private Metadata metadata;

    @Override
    public List<Person> getPersons() {
        LoadContext<Person> loadContext = LoadContext.create(Person.class).setQuery(
                LoadContext.createQuery("SELECT p FROM passportnumber$Person p")).setView("_local");
        List<Person> rez =  dataManager.loadList(loadContext);
        if (rez.size() == 0)
            throw new CustomValidationException("There are no persons in the database");

        return rez;
    }

    @Override
    public void addNewPerson(String name, BigDecimal height, CountryCode country, String passportNumber) {
        Person person = metadata.create(Person.class);
        person.setName(name);
        person.setHeight(height);
        person.setCountry(country);
        person.setPassportNumber(passportNumber);

        dataManager.commit(person);
    }
}