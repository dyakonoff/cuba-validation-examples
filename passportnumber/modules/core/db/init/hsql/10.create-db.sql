-- begin PASSPORTNUMBER_PERSON
create table PASSPORTNUMBER_PERSON (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    EMAIL varchar(120),
    COUNTRY integer not null,
    PASSPORT_NUMBER varchar(15) not null,
    --
    primary key (ID)
)^
-- end PASSPORTNUMBER_PERSON
