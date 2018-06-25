package com.company.passportnumber.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum CountryCode implements EnumClass<Integer> {

    AT(10),
    BE(20),
    HR(30),
    BG(40),
    CY(50),
    CZ(60),
    DK(70),
    EE(80),
    FI(90),
    FR(100),
    DE(110),
    GR(120),
    HU(130),
    IE(140),
    IT(150),
    LV(160),
    LT(170),
    LU(180),
    MT(190),
    NL(200),
    PL(210),
    PT(220),
    RO(230),
    SK(240),
    SI(250),
    ES(260),
    SE(270),
    GB(280);

    private Integer id;

    CountryCode(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static CountryCode fromId(Integer id) {
        for (CountryCode at : CountryCode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}