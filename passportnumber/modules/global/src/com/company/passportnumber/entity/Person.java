package com.company.passportnumber.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.global.validation.groups.UiCrossFieldChecks;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

@NamePattern("%s|name")
@Table(name = "PASSPORTNUMBER_PERSON")
@Entity(name = "passportnumber$Person")
@ValidPassportNumber(groups = {Default.class, UiCrossFieldChecks.class})
public class Person extends StandardEntity {
    private static final long serialVersionUID = -9150857881422152651L;

    @Pattern(message = "Bad formed person name: ${validatedValue}",
            regexp = "^[A-Z][a-z]*(\\s(([a-z]{1,3})|(([a-z]+\\')?[A-Z][a-z]*)))*$")
    @Length(min = 2)
    @NotNull
    @Column(name = "NAME", nullable = false)
    protected String name;

    @Email(message = "Email address has invalid format: ${validatedValue}",
            regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    @Column(name = "EMAIL", length = 120)
    protected String email;

    @DecimalMax(message = "Person height can not exceed 300 centimeters", value = "300")
    @DecimalMin(message = "Person height should be positive", value = "0", inclusive = false)
    @Column(name = "HEIGHT")
    protected BigDecimal height;

    @NotNull
    @Column(name = "COUNTRY", nullable = false)
    protected Integer country;

    @NotNull
    @Column(name = "PASSPORT_NUMBER", nullable = false, length = 15)
    protected String passportNumber;


    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getHeight() {
        return height;
    }


    public void setCountry(CountryCode country) {
        this.country = country == null ? null : country.getId();
    }

    public CountryCode getCountry() {
        return country == null ? null : CountryCode.fromId(country);
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}