package com.company.passportnumber.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s|name")
@Table(name = "PASSPORTNUMBER_PERSON")
@Entity(name = "passportnumber$Person")
public class Person extends StandardEntity {
    private static final long serialVersionUID = -9150857881422152651L;

    @NotNull
    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "EMAIL", length = 120)
    protected String email;

    @NotNull
    @Column(name = "COUNTRY", nullable = false)
    protected Integer country;

    @NotNull
    @Column(name = "PASSPORT_NUMBER", nullable = false, length = 15)
    protected String passportNumber;

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