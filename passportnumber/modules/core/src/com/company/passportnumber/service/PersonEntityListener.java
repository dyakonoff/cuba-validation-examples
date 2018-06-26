package com.company.passportnumber.service;

import com.company.passportnumber.entity.CountryCode;
import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.EntityManager;
import com.company.passportnumber.entity.Person;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.cuba.core.listener.BeforeUpdateEntityListener;

import javax.validation.ValidationException;


@Component("passportnumber_PersonEntityListener")
public class PersonEntityListener implements
        BeforeDeleteEntityListener<Person>,
        BeforeInsertEntityListener<Person>,
        BeforeUpdateEntityListener<Person> {


    /**
     * Checks that there are no other persons with the same passport number and country code
     * Ignores spaces in the passport number for the check.
     * So numbers "12 45 768007" and "1245 768007" and "1245768007" are the same for the validation purposes.
     * @param entity
     * @param entityManager
     */
    @Override
    public void onBeforeDelete(Person person, EntityManager entityManager) {
        if (!checkPassportIsUnique(person.getPassportNumber(), person.getCountry(), entityManager))
            throw new ValidationException("Passport and country code combination isn't unique");
    }


    /**
     * Checks that there are no other persons with the same passport number and country code
     * Ignores spaces in the passport number for the check.
     * So numbers "12 45 768007" and "1245 768007" and "1245768007" are the same for the validation purposes.
     * @param entity
     * @param entityManager
     */
    @Override
    public void onBeforeInsert(Person person, EntityManager entityManager) {
        // use entity argument to validate the Person object
        // entityManager could be used to access database if you need to check the data
        // throw ValidationException object if validation check failed
        if (!checkPassportIsUnique(person.getPassportNumber(), person.getCountry(), entityManager))
            throw new ValidationException("Passport and country code combination isn't unique");
    }

    /**
     * Checks that there are no other persons with the same passport number and country code
     * Ignores spaces in the passport number for the check.
     * So numbers "12 45 768007" and "1245 768007" and "1245768007" are the same for the validation purposes.
     * @param entity
     * @param entityManager
     */
    @Override
    public void onBeforeUpdate(Person person, EntityManager entityManager) {
        if (!checkPassportIsUnique(person.getPassportNumber(), person.getCountry(), entityManager))
            throw new ValidationException("Passport and country code combination isn't unique");
    }

    private boolean checkPassportIsUnique(String passportNumber, CountryCode country, EntityManager em) {
        // stub realization
        return true;
    }
}