package com.company.passportnumber.entity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidPassportNumberValidator implements ConstraintValidator<ValidPassportNumber, Person> {
   public void initialize(ValidPassportNumber constraint) {
   }

   public boolean isValid(Person person, ConstraintValidatorContext context) {
      if (person == null)
         return false;

      if (person.country == null || person.passportNumber == null)
         return false;

      return doPassportNumberFormatCheck(person.getCountry(), person.getPassportNumber());
   }

   private boolean doPassportNumberFormatCheck(CountryCode country, String passportNumber) {
      // dumb check that ensures that passport number is not empty and
      // contains only digits and spaces after trimming trailing and leading spaces
      Pattern pat = Pattern.compile("(^[\\d\\s]+$)", Pattern.CASE_INSENSITIVE);
      Matcher mat = pat.matcher(passportNumber.trim());
      return (passportNumber.trim().length() > 0) && mat.find();
   }
}
