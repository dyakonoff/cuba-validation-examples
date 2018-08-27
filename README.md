# Validation in Java applications

**This text discusses approaches to data validation. What are the common pitfalls that many projects fall into and what are the best practices Java applications should follow.**

## Content

1. [Introduction](#introduction)
1. [DB Constraints Validations](#db-constraints-validations)
1. [Bean Validation](#bean-validation)
1. [Validation by Contract](#validation-by-contract)
1. [Beyond Bean Validation](#beyond-bean-validation)
    * [Entity Listeners](#entity-listeners)
    * [Transaction Listeners](#transaction-listeners)
1. [Conclusion](#conclusion)
1. [References](#references)
    * [Standards & implementations](#standards--implementations)
    * [Libraries](#libraries)
    * [Validation ideology](#validation-ideology)
    * [Further reading](#further-reading)

## Introduction

I often have seen projects that didn’t have almost any conscious strategy for data validation. Their teams  worked under the great pressure of deadlines, unclear requirements and just didn’t have enough time to make validation in a proper and consistent way. So data validation code could be found everywhere: in Javascript snippets, Java screen controllers, business logic beans, domain model entities, database constraints and triggers. This code was full of if-else statements, was throwing different unchecked exceptions and it was just hard to find the right place where this damn piece of data could be validated... So, after a while, when project grew up enough it became quite hard and expensive to keep this validations consistent and following requirements, which as I’ve said are often fuzzy.

Is there a path to do data validation in an elegant, standard and concise way? The way that doesn’t fall ito a sin of unreadability, the way that helps us to keep most of the data validation logic together and which has most of the code already done for us by developers of popular Java frameworks?

Yes, there is.

For us, developers of [CUBA Platform](https://www.cuba-platform.com/), it is very important to let our users to follow the best practices. We believe that validation code should be:

1. Reusable and following DRY principle;
1. Expressed in a clear and natural way;
1. Placed in the place where developers expects it to see;
1. Able to check data from different data sources: user input, SOAP or REST calls etc.
1. Aware about concurrency;
1. Called implicitly by the application, without need to call the checks manually;
1. Showing clear, localized messages to a user using concise designed dialogs;
1. Following standards.

In this article I’ll be using an application based on CUBA Platform for all the examples. However, since CUBA is based on Spring and EclipseLink, most of this examples will work for any other Java framework that supports JPA and bean validation standard.

[Top](#content)

## DB Constraints Validations

Perhaps, the most common and straightforward way of data validation uses DB-level constraints, such as required flag (‘not null’ fields), string length, unique indexes and so on. This way is very natural for enterprise applications, as this class of software is usually heavily data-centric. However, even here developers often do mistakes, defining constraints separately for each tier of an application. This problem is often caused by splitting responsibilities between developers. 

Let's take an example most of you faced with, or even participated :). If a spec says that the passport field should have 10 digits in its number, most probably it will be checked everywhere: by DB architect in DDL, by backend developer in the corresponding Entity and REST services, finally, by UI developer right in client source-code. Later on this requirement changes and size of the field grows up to 15 digits. Tech Support changes the DB constraint, but for a user it means nothing as the client side check will not be passed anyway...

Everybody knows the way to avoid this problem, validations must be centralized! In CUBA this central point of such kind of validation is JPA annotations over entities. Based on this meta information, CUBA Studio generates right DDL scripts and applies corresponding validators on the client side.

![jpa_constraints_2](resources/jpa_constraints_2.png)

If JPA annotations get changed, CUBA updates DDL scripts and generate migration scripts, so next time you deploy your project, new JPA-based limitations will be applied to your application’s UI and DB.

Despite simplicity and implementation that spans up to DB level, and so is completely bullet-proof, JPA annotations are limited by the simplest cases that can be expressed in DDL standard without involving DB-specific triggers or stored procedures. So, JPA-based constraints can ensure that entity field is unique or mandatory or can define maximum length for a `varchar` column. Also, you can define unique constraint to the combination of columns with `@UniqueConstraint` annotation. But this is pretty much it.

However, in the cases that require more complex validation logic like checking for maximum and minimum values of a field or validating with a expression or doing a custom check that is specific to you application we need to utilize the well known approach called **“Bean Validation”**.

[Top](#content)

## Bean Validation

All we know, that it is a good practice to follow standards, which normally have long lifecycle and are battle-proven on thousands of projects. Java Bean validation is an approach that is set in stone in [JSR 380, 349 and 303](https://beanvalidation.org/specification/) and their implementations: [Hibernate Validator](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/?v=5.3) and [Apache BVal](http://bval.apache.org/).

Although this approach is familiar to many developers, it’s benefits are often underestimated. This is an easy way to add data validations even for legacy projects which allows you to express your validations in a clear, straightforward and reliable way as close to your business logic as it could be.

Using Bean Validation approach brings quite a lot benefits to your project:

* Validation logic is concentrated near your domain model: defining value, method, bean constraint is done in a natural way that allows to bring OOP approach to the next level.
* Bean Validation standard gives you tens of [validation annotations out of the box](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#validator-defineconstraints-spec), like: `@NotNull`, `@Size`, `@Min`, `@Max`, `@Pattern`, `@Email`, `@Past`, less standard like `@URL`, `@Length`, mighty `@ScriptAssert` and many others.
* You are not limited by predefined constraints and can define your own constraint annotations. You can make a new annotation also, by combining couple others or make a brand new one and define a Java class that will be served as a validator.<br />For example, looking at our previous example we can define a class-level annotation `@ValidPassportNumber` to check that passport number follows right format which depends from the `country` field value.
* You can put constraints not just on fields and classes, but also on methods and method parameters. This is called **“validation by contract”** and is the topic of the later section.

[CUBA Platform](https://www.cuba-platform.com/) (as some other frameworks) calls these bean validations automatically when user submits the data, so user would get the error message instantly if validation fails and you don’t need to worry about running these bean validators manually.

Let’s take a look at the passport number example once again, but this time we’d like to add couple additional constraints on the entity:

* Person name should have length of 2 or more and be a well-formed name. Regexp is quite complex, but *"Charles Ogier de Batz de Castelmore Comte d'Artagnan"* passes the check and *R2D2* does not :) ;
* Person height should be in interval: 0 < height <= 300 centimeters;
* Email string should be a properly formatted email address.

So, with all these checks the Person class looks like this:

```java
package com.company.passportnumber.entity;

...

@Listeners("passportnumber_PersonEntityListener")
@NamePattern("%s|name")
@Table(name = "PASSPORTNUMBER_PERSON")
@Entity(name = "passportnumber$Person")
@ValidPassportNumber(groups = {Default.class, UiCrossFieldChecks.class})
@FraudDetectionFlag
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

    ...
}
```

[Person.java](passportnumber/modules/global/src/com/company/passportnumber/entity/Person.java)

I think that usage of standard annotations like `@NotNull`, `@DecimalMin`, `@Length`, `@Pattern` and others is quite clear and doesn’t need a lot of comments. Let’s see how custom `@ValidPassportNumber` annotation is implemented.

Our brand new `@ValidPassportNumber` checks that `Person#passportNumber` match the regexp pattern specific to each country defined by `Person#country`.

First, following the documentation ([CUBA](https://doc.cuba-platform.com/manual-latest/bean_validation_constraints.html#bean_validation_custom_constraints) or [Hibernate](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-class-level-constraints) docs are good references), we need mark our entity class with this new annotation and pass groups parameter to it, where `UiCrossFieldChecks.class` says that the check should be called after checking all individual fields on the cross-field check stage and `Default.class` keeps the constraint in the default validation group.

The annotation definition looks like this:

```java
package com.company.passportnumber.entity;

...

/**
 * Checks that passport number is valid
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPassportNumberValidator.class)
public @interface ValidPassportNumber {
    String message() default "Passport number is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

[ValidPassportNumber](passportnumber/modules/global/src/com/company/passportnumber/entity/ValidPassportNumber.java)

`@Target(ElementType.TYPE)` defines that the target of this runtime annotation is a class and `@Constraint(validatedBy = … )` states that the annotation implementation is in `ValidPassportNumberValidator` class that implements `ConstraintValidator<...>` interface and has the validation code in `isValid(...)` method, which code does the actual check in a quite straightforward way:

```java
package com.company.passportnumber.entity;

...

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
        ...
    }
}
```

[ValidPassportNumberValidator.java](passportnumber/modules/global/src/com/company/passportnumber/entity/ValidPassportNumberValidator.java)

That’s it. With CUBA platform we don’t need to write a line of code more than that to get our custom validation working and giving messages to a user if he/she made a mistake.
Nothing complex, do you agree?

Now, let’s check how all this stuff work. CUBA has some extra goodies: it not just shows error messages to a user but also highlight form fields that hasn’t passed single-field bean validations with nice red lines:

![custom_validation_result](resources/custom_validation_result.png)

Isn’t this a neat thing? You got nice error UI feedback in the user's browser just after adding couple Java annotations to your domain model entities.

Concluding this section, let’s briefly list once again what pluses bean validation for entities has:

1. It is clear and readable;
1. It allows to define value constraints right in the domain classes;
1. It is extendable and customizable;
1. It is integrated with many popular ORMs and the checks are called automatically before changes are saved to a database;
1. Some frameworks also runs bean validation automatically when user submits data in UI (but if not, it’s not hard to call Validator interface manually);
1. Bean validation is a well-known standard, so there is a lot of documentation in the Internet about it.

But what shall we do if we need to set constraint onto a method, a constructor or some REST endpoint to validate data coming from external system? Or if we want to check the method parameters values in a declarative way without writing boring code full of if-elses in an each method we need to have such check?

The answer is simple: bean validation can be applied to methods as well!

[Top](#content)

## Validation by Contract

Sometimes, we need to make another step and go beyond just application data model state validation. Many methods might benefit from automatic parameters and return values validation. This might be required not just when we need to check data coming to a REST or SOAP endpoint but also when we want to express preconditions and postconditions for method calls to be sure that input data have been checked before method body executes or that the return values are in the expected range, or we just want to declaratively express parameters boundaries for better readability.

With bean validation, constraints can be applied to the parameters and return values of a method or constructors of any Java type to check for their calls preconditions and postconditions. This approach has several advantages over traditional ways of checking the correctness of parameters and return values:

1. The checks don’t need to be performed manually in the imperative way (e.g. by throwing `IllegalArgumentException` or similar). We rather specify constraints declaratively, so we have more readable and expressive code;
1. Constraints are reusable, configurable and customizable: we don’t need to write validation code every time we need to do the checks. Less code - less bugs.
1. If a class or method return value or method parameter is marked with `@Validated` annotation, that the constraints check would be done automatically by the framework on every method call.
1. If an executable is marked with `@Documented` annotation then it’s pre- and post- conditions would be included in the generated JavaDoc.

As the result with the **‘validation by contract’** approach we have clear code, less amount of it which it is easier to support and understand.

Let’s look how does it looks like for a REST controller interface in the CUBA app. `PersonApiService` interface allows to get a list of persons from the DB with `getPersons()` method and to add a new person to the DB using `addNewPerson(...)` call. And remember: bean validation is inheritable! In other words, if you annotate some class or field or method with a constraint, all descendants that extends or implements this class or interface would be affected by the same constraint check.

```java
package com.company.passportnumber.service;

...

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
```

[PersonApiService.java](passportnumber/modules/global/src/com/company/passportnumber/service/PersonApiService.java)

Does this code snippet look pretty clear and readable for you?<br />
*(With the exception of `@RequiredView(“_local”)` annotation which is specific for CUBA platform and checks that returned Person object has all fields loaded from the `PASSPORTNUMBER_PERSON` table).*<br />
`@Valid` annotation specifies that every object in the collection returned by `getPersons()` method need to be validated against Person class constraints as well.

CUBA makes these methods available at the next endpoints:

* `/app/rest/v2/services/passportnumber_PersonApiService/getPersons`
* `/app/rest/v2/services/passportnumber_PersonApiService/addNewPerson`

Let’s open the Postman app and ensure that validation works as expected:

![rest_validation_result](resources/rest_validation_result.png)

You might have noted that the above example doesn’t validate passport number. This is because it requires cross-parameter validation of the `addNewPerson` method since `passportNumber` validation regexp pattern depends from the `country` value. Such cross parameter checks are direct equivalent to class-level constraints for entities!

Cross parameter validation is supported by JSR 349 and 380, you can consult [hibernate documentation](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-cross-parameter-constraints) for how to implement custom cross-parameter validators for class / interface methods.

[Top](#content)

## Beyond Bean Validation

Nothing is perfect in the world, and bean validation has has some limitations as well:

1. Sometime you just want to validate an complex object graph state before saving changes to the database. For example you might need to ensure that all items from an order made by a customer of  your e-commerce system could be fit in one of the shipping boxes you have. This is quite heavy operation amd doing such check every time users add new items to their orders isn’t the best idea. Hence, such check might need to be called just once before the Order object and it’s OrderItem objects are saved to the database.
1. Some checks have to be made inside the transaction. For example, e-commerce system should check if there are enough items in stock to fulfill the order before committing it to the database. Such check could be done only from inside the transaction, because the system is concurrent and quantities in stock could be changed at any time.

CUBA platform offers two mechanisms to validate data before commit which are called [entity listeners](https://doc.cuba-platform.com/manual-latest/entity_listeners.html) and [transaction listeners](https://doc.cuba-platform.com/manual-latest/transaction_listeners.html). Let’s look at them a bit more closely.

[Top](#content)

### Entity Listeners

[Entity listeners in CUBA](https://doc.cuba-platform.com/manual-latest/entity_listeners.html) are quite similar to [`PreInsertEvent`, `PreUpdateEvent` and  `PredDeleteEvent` listeners](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#validator-checkconstraints-orm-hibernateevent) that JPA offers to a developer. Both mechanisms allow to check entity objects before or after they get persisted to a database. 

It’s not hard to define and wire up an entity listener in CUBA, we need to do two things:

1. Create a managed bean that implements one of the entity listener interfaces. For validation purposes 3 of these interfaces are important: <br />`BeforeDeleteEntityListener<T>`, <br />`BeforeInsertEntityListener<T>` and <br />`BeforeUpdateEntityListener<T>`
1. Annotate the entity object that plan to track with `@Listeners` annotation.

That’s it.

In comparison with JPA standard (JSR 338, chapter 3.5) CUBA platform’s listener interfaces are typed, so you don’t need to cast Object argument to start working with the entity.  CUBA platform adds possibility of entities associated with the current one or calling `EntityManager` to load and change any other entities. All such changes would invoke appropriate entity listener calls as well.

Also CUBA platform supports [soft deletion](https://doc.cuba-platform.com/manual-latest/soft_deletion.html), a feature when entities in DB are just marked as deleted without deleting their records from the DB. So, for soft deletion CUBA platform would call `BeforeDeleteEntityListener` / `AfterDeleteEntityListener` listeners while standard implementations would call `PreUpdate` / `PostUpdate` listeners.

Let’s look at the example. Event listener bean connects to an Entity class with just one line of code: annotation `@Listeners` that accepts a name of the entity listener class:

```java
package com.company.passportnumber.entity;

...

@Listeners("passportnumber_PersonEntityListener")
@NamePattern("%s|name")
@Table(name = "PASSPORTNUMBER_PERSON")
@Entity(name = "passportnumber$Person")
@ValidPassportNumber(groups = {Default.class, UiCrossFieldChecks.class})
@FraudDetectionFlag
public class Person extends StandardEntity {
    ...
}
```

[Person.java](passportnumber/modules/global/src/com/company/passportnumber/entity/Person.java)

And entity listener implementation may look like this:

```java
package com.company.passportnumber.service;

...

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
        ...
    }
}
```

[PersonEntityListener.java](passportnumber/modules/core/src/com/company/passportnumber/service/PersonEntityListener.java)

Entity listeners are great choice when you:

* Need to make data check inside transaction before the entity object get persisted to a DB;
* Need to check data in the DB during the validation process, for example check that we have enough goods in stock to accept the order;
* Need to traverse not just given entity object, like `Order`, but visit the object that are in the association or composition with the entity, like `OrderItems` objects for the `Order` entity;
* Want to track insert / update / delete operations for just some of your entity classes, for example you want to track such events only for `Order` and `OrderItem` entities, and don’t need to validate changes in other entity classes during transaction.

[Top](#content)

### Transaction Listeners

[CUBA transaction listener’s](https://doc.cuba-platform.com/manual-latest/transaction_listeners.html) works in transactional context as well, but in comparison with entity listeners they get called for **every** database transaction.

This gives them the ultimate power:

* nothing can pass their attention,

but the same gives them weaknesses:

* they are harder to write,
* they can downgrade performance significantly if performing too much unneeded checks,
* They need to be written much more careful: a bug in transaction listener might even prevent application from bootstrapping;

So, transaction listeners is a good solution when you need to inspect many different type of entities with the same algorithm, like feeding data to a custom fraud detector that serves all your business objects.

~[You shall not pass!](resources/gendalf.png)

Let’s look at the example that checks if an entity is annotated with `@FraudDetectionFlag` annotation and if yes, runs the fraud detector to validate it. Once again, please note, that this method is called **before every DB transaction gets committed** in the system, so the code has to try to check as least objects as possible as fast as it can.

```java
package com.company.passportnumber.service;

...

@Component("passportnumber_ApplicationTransactionListener")
public class ApplicationTransactionListener implements BeforeCommitTransactionListener {

    private Logger log = LoggerFactory.getLogger(ApplicationTransactionListener.class);

    @Override
    public void beforeCommit(EntityManager entityManager, Collection<Entity> managedEntities) {
        for (Entity entity : managedEntities)
            if (entity instanceof StandardEntity
                    && !((StandardEntity) entity).isDeleted()
                    && entity.getClass().isAnnotationPresent(FraudDetectionFlag.class)
                    && !fraudDetectorFeedAndFastCheck(entity))
            {
                logFraudDetectionFailure(log, entity);
                String msg = String.format("Fraud detection failure in '%s' object with id = '%s'",
                        entity.getClass().getSimpleName(), entity.getId() );
                throw new ValidationException(msg);
            }
    }

    ...
}
```

[ApplicationTransactionListener.java](passportnumber/modules/core/src/com/company/passportnumber/service/ApplicationTransactionListener.java)

To become a transaction listener, managed bean should just implement `BeforeCommitTransactionListener` interface and implement `beforeCommit` method. Transaction listeners are wired up automatically when the application starts. CUBA registers all classes that implements `BeforeCommitTransactionListener` or `AfterCompleteTransactionListener` as transaction listeners.

[Top](#content)

## Conclusion

Bean validation [(JPA 303, 349 and 980)](https://beanvalidation.org/specification/) is an approach that could serve as a concrete foundation for 95% of the data validation cases that happen in an enterprise project. The big advantage of such approach is that most of your validation logic is concentrated right in your domain model classes. So it is easy to be found, easy to be read and be supported. Spring, CUBA and many libraries are aware about these standards and calls the validation checks automatically during UI input, validated method calls or ORM persistence process, so validation works like a charm from developer’s perspective.

Some software engineers see validation that impacts an applications domain models as being somewhat invasive and complex, they say that making data checks at UI level is a good enough strategy. However, I believe that having multiple validation points in UI controls and controllers is quite problematic approach. In addition, validation methods we have discussed here are not perceived as invasive when they integrate with a framework that is aware about bean validators, listeners and integrates them to the client level automatically.

At the end, let’s formulate a rule of thumb to choose the best validation method:

* **JPA validation** has limited functionality, but is a great choice for simplest constraints on entity classes if such constraints can be mapped to DDL.
* **Bean Validation** is flexible, concise, declarative, reusable and readable way to cover most of the checks that  your could have in your domain model classes. This is the best choice in most cases once you don’t need to run validations inside a transaction.
* **Validation by Contract** is a bean validation but for method calls. Use it when you need to check input and output parameters of a method, for example in a REST call handler.
* **Entity listeners**: although they are not such declarative as Bean Validation annotations, they are great place to check big object’s graphs or make a check that needs to be done inside a database transaction. For example, when you need to read some data from the DB to make a decision. Hibernate has analogs of such listeners.
* **Transaction listeners** are dangerous but ultimate weapon that works inside transactional context. Use it when you need to decide at runtime what objects have to be validated or when you need to check many different types of your entities against the same validation algorithm.

I hope that this article refreshed your memories about different validation methods available in Java enterprise applications and gave you couple ideas how to improve architecture of the projects you are working on.

[Top](#content)

---

## References

### Standards & implementations

* [JSR 303 - Bean Validation 1.0](https://beanvalidation.org/1.0/)
* [JSR 349 - Bean Validation 1.1](https://beanvalidation.org/1.1/)
* [JSR 349, Bean Validation Specification](https://beanvalidation.org/1.1/spec/)
* [JSR 380, Bean Validation Specification](https://beanvalidation.org/2.0/spec/)
* [Hibernate Validator 5.4.2 (JSR 349) reference](https://docs.jboss.org/hibernate/validator/5.4/reference/en-US/html_single/)
* [Hibernate Validator 6.10 (JSR 380) reference](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/)
* [Hibernate Validator main page](http://hibernate.org/validator/)
* [Hibernate validator 6.10 API docs](https://docs.jboss.org/hibernate/stable/validator/api/)
* [HV 6.10: Declaring and validating method constraints](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#chapter-method-constraints)
* [HV 6.10: Cross parameter constraints](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-cross-parameter-constraints)

### Libraries

* [CUBA bean validation](https://doc.cuba-platform.com/manual-6.9/bean_validation.html)
* [Validation cookbook for CUBA applications](https://github.com/dyakonoff/cuba-validation)
* [JavaFX with Bean Validation and CDI 2.0](http://fxapps.blogspot.com/2017/10/javafx-with-bean-validation-and-cdi-20.html)
* [OVal - non a JSR 303, 349, 380 compliant validation framework](http://oval.sourceforge.net/)
* [Spring, Java Bean Validation Basics](http://www.baeldung.com/javax-validation)
* [Validation, Data Binding, and Type Conversion in Spring 4.1](https://docs.spring.io/spring/docs/4.1.x/spring-framework-reference/html/validation.html)

### Validation ideology

* [Form validation best practices](https://medium.com/@andrew.burton/form-validation-best-practices-8e3bec7d0549)
* [JavaFX Form Validation](http://mail.openjdk.java.net/pipermail/openjfx-dev/2012-June/002361.html)
* [Blah vs Bean Validation: you missed the point like Mars Climate Orbiter](http://in.relation.to/2014/06/19/blah-vs-bean-validation-you-missed-the-point-like-mars-climate-orbiter/)
* [Avoiding Many If Blocks For Validation Checking](https://dzone.com/articles/avoiding-many-if-blocks)

### Further reading

* [Validation cookbook for CUBA applications](https://github.com/dyakonoff/cuba-validation)

[Top](#content)