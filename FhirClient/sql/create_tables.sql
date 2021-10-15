create table if not exists member (
    id int not null,
    status varchar,
    first_name varchar(100),
    last_name  varchar(100),
    date_of_birth date,
    gender char,
    address_line_1 varchar,
    address_line_2 varchar,
    address_line_3 varchar,
    city varchar,
    state varchar,
    zipcode varchar,
    country varchar,
    sponsor_id bigint,
    PRIMARY KEY (id)
);