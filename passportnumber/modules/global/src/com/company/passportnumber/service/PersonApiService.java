package com.company.passportnumber.service;

import com.company.passportnumber.entity.CountryCode;
import com.company.passportnumber.entity.Person;
import com.haulmont.cuba.core.global.validation.RequiredView;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@Validated
public interface PersonApiService {
    String NAME = "passportnumber_PersonApiService";

    @NotNull
    @Valid
    @RequiredView("_local")
    List<Person> getPersons();

    void addNewPerson(@NotNull
                      @Length(min = 2, max = 255)
                      @Pattern(message = "Bad formed person name: ${validatedValue}",
                              regexp = "^[A-Z][a-z]*(\\s(([a-z]{1,3})|(([a-z]+\\')?[A-Z][a-z]*)))*$")
                          String name,
                      @DecimalMax(message = "Person height can not exceed 300 centimeters", value = "300")
                      @DecimalMin(message = "Person height should be positive", value = "0", inclusive = false)
                          BigDecimal height,
                      @NotNull
                          CountryCode country,
                      @NotNull
                      String passportNumber
                      );
}