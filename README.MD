
Skip to end of metadata
Created by Rasanjalee Dissanayaka M. Dissanayaka Mudiyanselage, last modified on Mar 20, 2020, viewed 35 timesGo to start of metadata
This is a poc to test how debezium connector works with postgres using docker:



1. Start a PostgreSQL instance:
2. Create database, member table
3. Start a Zookeeper instance:
4. Start a Kafka instance:
5. Start a Debezium kafka connect instance:
6. deploy postgres connector
7. View load event
8. View update event
9. View insert event
10. View delete event

## 1.Start a PostgreSQL instance:
    docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres debezium/example-postgres:1.0


## 2. Create database, member table
```shell
docker exec -it postgres psql -U postgres

postgres # CREATE DATABASE genesis;

postgres # \c genesis

genesis #

CREATE TABLE public.member (
id bigint NOT NULL,
first_name character varying(100),
last_name character varying(100),
display_name character varying(100),
profile_picture character varying,
date_of_birth date,
email_address character varying(255) NOT NULL,
is_deleted boolean DEFAULT false NOT NULL,
vhm_legacy_id bigint NOT NULL,
created_date timestamp with time zone DEFAULT now() NOT NULL,
updated_date timestamp with time zone,
gender character(1),
background_image character varying(255),
words_to_live_by character varying(255),
my_bio text,
sponsor_id bigint,
timezone_id bigint,
language character varying(15),
unit_of_measurement character varying(50),
business_unit_id bigint,
office_id bigint,
is_wizard_form_completed boolean DEFAULT false,
is_internal boolean DEFAULT false,
member_settings_id bigint NOT NULL,
status character varying(50) DEFAULT 'Active'::character varying NOT NULL,
title character varying(50),
department character varying(50),
location character varying(50),
type character varying(50) DEFAULT 'Primary'::character varying NOT NULL,
country character varying(3),
hire_date date,
original_hire_date date,
employee_id character varying(250),
division character varying(100),
failed_security_question_count integer,
security_question_lockout boolean,
country_id bigint,
user_name character varying,
phone_number character varying(30),
gender_identity character varying(255),
external_id character varying(32) DEFAULT md5(((random())::text || (clock_timestamp())::text)),
state_id bigint,
enrollment_date timestamp with time zone
);

genesis #
ALTER TABLE ONLY public.member ADD CONSTRAINT pk_member PRIMARY KEY (id);

genesis #
INSERT INTO public.member (id, first_name, last_name, display_name, profile_picture, date_of_birth, email_address, is_deleted, vhm_legacy_id, created_date, updated_date, gender, background_image, words_to_live_by, my_bio, sponsor_id, timezone_id, language, unit_of_measurement, business_unit_id, office_id, is_wizard_form_completed, is_internal, member_settings_id, status, title, department, location, type, country, hire_date, original_hire_date, employee_id, division, failed_security_question_count, security_question_lockout, country_id, user_name, phone_number, gender_identity, external_id, state_id, enrollment_date)
VALUES (141418, 'Jasenko', 'Hadziomeragic', 'Jasenko Hadziomeragic', 'https://mashauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif', '1988-04-05', 'test111@live.com', false, 133227, '2014-12-16 15:39:24.866+00', '2019-12-13 16:12:52.555+00', 'M', NULL, NULL, NULL, 38383, 35, 'en_us', 'Imperial', NULL, NULL, NULL, false, 8606, 'Cancelled', NULL, NULL, NULL, 'Primary', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Male', '46d790dc7e7283c169e5fa201d7e39ee', NULL, '2014-12-16 15:39:24.866+00');
```

## 3. Start a Zookeeper instance:
   docker run -it --rm --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 --expose 2181 debezium/zookeeper:1.0

## 4. Start a Kafka instance:
   docker run -it --rm --name kafka -p 9092:9092 --expose 9092 --link zookeeper:zookeeper debezium/kafka:1.0

ouptput: **Note KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.17.0.4:9092**
```shell
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/PostgresToKafkaProducer  docker run -it --rm --name kafka -p 9092:9092 --expose 9092 --link zookeeper:zookeeper debezium/kafka:1.0
WARNING: Using default BROKER_ID=1, which is valid only for non-clustered installations.
Using ZOOKEEPER_CONNECT=172.17.0.3:2181
Using KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.17.0.4:9092
2021-10-06 16:55:52,973 - INFO  [main:Log4jControllerRegistration$@31] - Registered kafka:type=kafka.Log4jController MBean
2021-10-06 16:55:53,458 - INFO  [main:LoggingSignalHandler@72] - Registered signal handlers for TERM, INT, HUP
2021-10-06 16:55:53,459 - INFO  [main:Logging@66] - starting
```

## 5. Start a Debezium kafka connect instance:
   docker run -it --rm --name connect -p 8083:8083 -e GROUP_ID=1 -e CONFIG_STORAGE_TOPIC=my-connect-configs -e OFFSET_STORAGE_TOPIC=my-connect-offsets -e ADVERTISED_HOST_NAME=$(echo $DOCKER_HOST | cut -f3 -d'/' | cut -f1 -d':') --expose 8083 --link zookeeper:zookeeper --link postgres:postgres --link kafka:kafka debezium/connect:1.0


## 6. deploy postgres connector
   curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d ' { "name": "genesis-connector", "config": { "connector.class": "io.debezium.connector.postgresql.PostgresConnector", "tasks.max": "1", "database.hostname": "postgres", "database.port": "5432", "database.user": "postgres", "database.password": "postgres", "database.dbname" : "genesis", "database.server.name": "dbserver1", "database.whitelist": "genesis", "database.history.kafka.bootstrap.servers": "kafka:9092", "database.history.kafka.topic": "schema-changes.genesis" } }'


output:
```json
{
  "name":"genesis-connector",
  "config":     
    {
      "connector.class":"io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max":"1",
      "database.hostname":"postgres",
      "database.port":"5432",
      "database.user":"postgres",
      "database.password":"postgres",
      "database.dbname":"genesis",
      "database.server.name":"dbserver1",
      "database.whitelist":"genesis",
      "database.history.kafka.bootstrap.servers":"kafka:9092",
      "database.history.kafka.topic":"schema-changes.genesis",
      "name":"genesis-connector"
    },
  "tasks":[],
  "type":"source"
}%
```

Verify the Connector is created:

```shell
curl -X GET -H "Accept:application/json" localhost:8083/connectors/genesis-connector
```

output:
```json
{"name":"genesis-connector","config":{"connector.class":"io.debezium.connector.postgresql.PostgresConnector","database.user":"postgres","database.dbname":"genesis","tasks.max":"1","database.hostname":"postgres","database.password":"postgres","database.history.kafka.bootstrap.servers":"kafka:9092","database.history.kafka.topic":"schema-changes.genesis","name":"genesis-connector","database.server.name":"dbserver1","database.whitelist":"genesis","database.port":"5432"},"tasks":[{"connector":"genesis-connector","task":0}],"type":"source"}%

```


## 7. start a Kafka Console consumer to watch changes:

```shell
docker run -it --rm --name watcher --link zookeeper:zookeeper --link kafka:kafka debezium/kafka:1.0 watch-topic -a -k dbserver1.public.member
```
This will print all ingestion events below:

## 8. View load event
   Once watcher is run Y\you can now see load of (op: "r") of first load table records. each record has two json strings printed(one for key, one for value. payload field has id related to record being transferred. this contains before and after values ).



### LOAD

```json
//RECORD 1: key
{
"schema": {
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
}
],
"optional": false,
"name": "dbserver1.public.member.Key"
},
"payload": {
"id": 141418
}
}

//RECORD 1: value

{
"schema": {
"type": "struct",
"fields": [
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "before"
},
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "after"
},
{
"type": "struct",
"fields": [
{
"type": "string",
"optional": false,
"field": "version"
},
{
"type": "string",
"optional": false,
"field": "connector"
},
{
"type": "string",
"optional": false,
"field": "name"
},
{
"type": "int64",
"optional": false,
"field": "ts_ms"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.data.Enum",
"version": 1,
"parameters": {
"allowed": "true,last,false"
},
"default": "false",
"field": "snapshot"
},
{
"type": "string",
"optional": false,
"field": "db"
},
{
"type": "string",
"optional": false,
"field": "schema"
},
{
"type": "string",
"optional": false,
"field": "table"
},
{
"type": "int64",
"optional": true,
"field": "txId"
},
{
"type": "int64",
"optional": true,
"field": "lsn"
},
{
"type": "int64",
"optional": true,
"field": "xmin"
}
],
"optional": false,
"name": "io.debezium.connector.postgresql.Source",
"field": "source"
},
{
"type": "string",
"optional": false,
"field": "op"
},
{
"type": "int64",
"optional": true,
"field": "ts_ms"
}
],
"optional": false,
"name": "dbserver1.public.member.Envelope"
},
"payload": {
"before": null,
"after": {
"id": 141418,
"first_name": "Jasenko",
"last_name": "Hadziomeragic",
"display_name": "Jasenko Hadziomeragic",
"profile_picture": "https://mashauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif",
"date_of_birth": 6669,
"email_address": "test111@live.com",
"is_deleted": false,
"vhm_legacy_id": 133227,
"created_date": "2014-12-16T15:39:24.866Z",
"updated_date": "2019-12-13T16:12:52.555Z",
"gender": "M",
"background_image": null,
"words_to_live_by": null,
"my_bio": null,
"sponsor_id": 38383,
"timezone_id": 35,
"language": "en_us",
"unit_of_measurement": "Imperial",
"business_unit_id": null,
"office_id": null,
"is_wizard_form_completed": null,
"is_internal": false,
"member_settings_id": 8606,
"status": "Cancelled",
"title": null,
"department": null,
"location": null,
"type": "Primary",
"country": null,
"hire_date": null,
"original_hire_date": null,
"employee_id": null,
"division": null,
"failed_security_question_count": null,
"security_question_lockout": null,
"country_id": null,
"user_name": null,
"phone_number": null,
"gender_identity": "Male",
"external_id": "46d790dc7e7283c169e5fa201d7e39ee",
"state_id": null,
"enrollment_date": "2014-12-16T15:39:24.866Z"
},
"source": {
"version": "1.0.2.Final",
"connector": "postgresql",
"name": "dbserver1",
"ts_ms": 1584551957919,
"snapshot": "last",
"db": "genesis",
"schema": "public",
"table": "member",
"txId": 605,
"lsn": 34198640,
"xmin": null
},
"op": "r",
"ts_ms": 1584551957926
}
}
```

## 9. View update event

connect to database and execute following:


docker exec -it postgres psql -U postgres

\c genesis

UPDATE member SET first_name='Jasenk', last_name='Hadziomeragic M' WHERE id=141418


output from watcher (op: u for update):


```json
//RECORD 1: key
{
"schema": {
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
}
],
"optional": false,
"name": "dbserver1.public.member.Key"
},
"payload": {
"id": 141418
}
}

//RECORD 1: value
{
"schema": {
"type": "struct",
"fields": [
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "before"
},
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "after"
},
{
"type": "struct",
"fields": [
{
"type": "string",
"optional": false,
"field": "version"
},
{
"type": "string",
"optional": false,
"field": "connector"
},
{
"type": "string",
"optional": false,
"field": "name"
},
{
"type": "int64",
"optional": false,
"field": "ts_ms"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.data.Enum",
"version": 1,
"parameters": {
"allowed": "true,last,false"
},
"default": "false",
"field": "snapshot"
},
{
"type": "string",
"optional": false,
"field": "db"
},
{
"type": "string",
"optional": false,
"field": "schema"
},
{
"type": "string",
"optional": false,
"field": "table"
},
{
"type": "int64",
"optional": true,
"field": "txId"
},
{
"type": "int64",
"optional": true,
"field": "lsn"
},
{
"type": "int64",
"optional": true,
"field": "xmin"
}
],
"optional": false,
"name": "io.debezium.connector.postgresql.Source",
"field": "source"
},
{
"type": "string",
"optional": false,
"field": "op"
},
{
"type": "int64",
"optional": true,
"field": "ts_ms"
}
],
"optional": false,
"name": "dbserver1.public.member.Envelope"
},
"payload": {
"before": null,
"after": {
"id": 141418,
"first_name": "Jasenk",
"last_name": "Hadziomeragic M",
"display_name": "Jasenko Hadziomeragic",
"profile_picture": "https://mashauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif",
"date_of_birth": 6669,
"email_address": "test111@live.com",
"is_deleted": false,
"vhm_legacy_id": 133227,
"created_date": "2014-12-16T15:39:24.866Z",
"updated_date": "2019-12-13T16:12:52.555Z",
"gender": "M",
"background_image": null,
"words_to_live_by": null,
"my_bio": null,
"sponsor_id": 38383,
"timezone_id": 35,
"language": "en_us",
"unit_of_measurement": "Imperial",
"business_unit_id": null,
"office_id": null,
"is_wizard_form_completed": null,
"is_internal": false,
"member_settings_id": 8606,
"status": "Cancelled",
"title": null,
"department": null,
"location": null,
"type": "Primary",
"country": null,
"hire_date": null,
"original_hire_date": null,
"employee_id": null,
"division": null,
"failed_security_question_count": null,
"security_question_lockout": null,
"country_id": null,
"user_name": null,
"phone_number": null,
"gender_identity": "Male",
"external_id": "46d790dc7e7283c169e5fa201d7e39ee",
"state_id": null,
"enrollment_date": "2014-12-16T15:39:24.866Z"
},
"source": {
"version": "1.0.2.Final",
"connector": "postgresql",
"name": "dbserver1",
"ts_ms": 1584552981723,
"snapshot": "false",
"db": "genesis",
"schema": "public",
"table": "member",
"txId": 606,
"lsn": 34198960,
"xmin": null
},
"op": "u",
"ts_ms": 1584552981822
}
}
```

## 10. View insert event

connect to database and execute following:

docker exec -it postgres psql -U postgres

\c genesis

INSERT INTO public.member (id, first_name, last_name, display_name, profile_picture, date_of_birth, email_address, is_deleted, vhm_legacy_id, created_date, updated_date, gender, background_image, words_to_live_by, my_bio, sponsor_id, timezone_id, language, unit_of_measurement, business_unit_id, office_id, is_wizard_form_completed, is_internal, member_settings_id, status, title, department, location, type, country, hire_date, original_hire_date, employee_id, division, failed_security_question_count, security_question_lockout, country_id, user_name, phone_number, gender_identity, external_id, state_id, enrollment_date)
VALUES (2265, 'Tony', 'Parker', 'Tony Parker', 'https://qaauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif', NULL, 'tonyPvP@gmail.com', false, 134295, '2014-05-07 12:53:43.799+00', '2014-05-07 12:53:43.799+00', 'M', NULL, NULL, NULL, NULL, 35, 'en_us', 'Imperial', NULL, NULL, true, false, 7928, 'Active', NULL, NULL, NULL, 'Primary', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Male', 'efd8cf4d5659733d42c54ba79db771be', NULL, '2014-05-07 12:53:43.799+00');


output from watcher (op: c for insert/create):

```json
//RECORD 2: key
{
"schema": {
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
}
],
"optional": false,
"name": "dbserver1.public.member.Key"
},
"payload": {
"id": 2265
}
}'

//RECORD 2: value
{
"schema": {
"type": "struct",
"fields": [
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "before"
},
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "after"
},
{
"type": "struct",
"fields": [
{
"type": "string",
"optional": false,
"field": "version"
},
{
"type": "string",
"optional": false,
"field": "connector"
},
{
"type": "string",
"optional": false,
"field": "name"
},
{
"type": "int64",
"optional": false,
"field": "ts_ms"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.data.Enum",
"version": 1,
"parameters": {
"allowed": "true,last,false"
},
"default": "false",
"field": "snapshot"
},
{
"type": "string",
"optional": false,
"field": "db"
},
{
"type": "string",
"optional": false,
"field": "schema"
},
{
"type": "string",
"optional": false,
"field": "table"
},
{
"type": "int64",
"optional": true,
"field": "txId"
},
{
"type": "int64",
"optional": true,
"field": "lsn"
},
{
"type": "int64",
"optional": true,
"field": "xmin"
}
],
"optional": false,
"name": "io.debezium.connector.postgresql.Source",
"field": "source"
},
{
"type": "string",
"optional": false,
"field": "op"
},
{
"type": "int64",
"optional": true,
"field": "ts_ms"
}
],
"optional": false,
"name": "dbserver1.public.member.Envelope"
},
"payload": {
"before": null,
"after": {
"id": 2265,
"first_name": "Tony",
"last_name": "Parker",
"display_name": "Tony Parker",
"profile_picture": "https://qaauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif",
"date_of_birth": null,
"email_address": "tonyPvP@gmail.com",
"is_deleted": false,
"vhm_legacy_id": 134295,
"created_date": "2014-05-07T12:53:43.799Z",
"updated_date": "2014-05-07T12:53:43.799Z",
"gender": "M",
"background_image": null,
"words_to_live_by": null,
"my_bio": null,
"sponsor_id": null,
"timezone_id": 35,
"language": "en_us",
"unit_of_measurement": "Imperial",
"business_unit_id": null,
"office_id": null,
"is_wizard_form_completed": true,
"is_internal": false,
"member_settings_id": 7928,
"status": "Active",
"title": null,
"department": null,
"location": null,
"type": "Primary",
"country": null,
"hire_date": null,
"original_hire_date": null,
"employee_id": null,
"division": null,
"failed_security_question_count": null,
"security_question_lockout": null,
"country_id": null,
"user_name": null,
"phone_number": null,
"gender_identity": "Male",
"external_id": "efd8cf4d5659733d42c54ba79db771be",
"state_id": null,
"enrollment_date": "2014-05-07T12:53:43.799Z"
},
"source": {
"version": "1.0.2.Final",
"connector": "postgresql",
"name": "dbserver1",
"ts_ms": 1584553306402,
"snapshot": "false",
"db": "genesis",
"schema": "public",
"table": "member",
"txId": 607,
"lsn": 34200376,
"xmin": null
},
"op": "c",
"ts_ms": 1584553306409
}
}
```

## 11. View delete event
connect to database and execute following:

docker exec -it postgres psql -U postgres

\c genesis

DELETE FROM public.member WHERE id=2265;


output from watcher (op: d for delete):

```json
//RECORD 2: key
{
"schema": {
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
}
],
"optional": false,
"name": "dbserver1.public.member.Key"
},
"payload": {
"id": 2265
}
}

//RECORD 2: value
{
"schema": {
"type": "struct",
"fields": [
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "before"
},
{
"type": "struct",
"fields": [
{
"type": "int64",
"optional": false,
"field": "id"
},
{
"type": "string",
"optional": true,
"field": "first_name"
},
{
"type": "string",
"optional": true,
"field": "last_name"
},
{
"type": "string",
"optional": true,
"field": "display_name"
},
{
"type": "string",
"optional": true,
"field": "profile_picture"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "date_of_birth"
},
{
"type": "string",
"optional": false,
"field": "email_address"
},
{
"type": "boolean",
"optional": false,
"field": "is_deleted"
},
{
"type": "int64",
"optional": false,
"field": "vhm_legacy_id"
},
{
"type": "string",
"optional": false,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "created_date"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "updated_date"
},
{
"type": "string",
"optional": true,
"field": "gender"
},
{
"type": "string",
"optional": true,
"field": "background_image"
},
{
"type": "string",
"optional": true,
"field": "words_to_live_by"
},
{
"type": "string",
"optional": true,
"field": "my_bio"
},
{
"type": "int64",
"optional": true,
"field": "sponsor_id"
},
{
"type": "int64",
"optional": true,
"field": "timezone_id"
},
{
"type": "string",
"optional": true,
"field": "language"
},
{
"type": "string",
"optional": true,
"field": "unit_of_measurement"
},
{
"type": "int64",
"optional": true,
"field": "business_unit_id"
},
{
"type": "int64",
"optional": true,
"field": "office_id"
},
{
"type": "boolean",
"optional": true,
"field": "is_wizard_form_completed"
},
{
"type": "boolean",
"optional": true,
"field": "is_internal"
},
{
"type": "int64",
"optional": false,
"field": "member_settings_id"
},
{
"type": "string",
"optional": false,
"field": "status"
},
{
"type": "string",
"optional": true,
"field": "title"
},
{
"type": "string",
"optional": true,
"field": "department"
},
{
"type": "string",
"optional": true,
"field": "location"
},
{
"type": "string",
"optional": false,
"field": "type"
},
{
"type": "string",
"optional": true,
"field": "country"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "hire_date"
},
{
"type": "int32",
"optional": true,
"name": "io.debezium.time.Date",
"version": 1,
"field": "original_hire_date"
},
{
"type": "string",
"optional": true,
"field": "employee_id"
},
{
"type": "string",
"optional": true,
"field": "division"
},
{
"type": "int32",
"optional": true,
"field": "failed_security_question_count"
},
{
"type": "boolean",
"optional": true,
"field": "security_question_lockout"
},
{
"type": "int64",
"optional": true,
"field": "country_id"
},
{
"type": "string",
"optional": true,
"field": "user_name"
},
{
"type": "string",
"optional": true,
"field": "phone_number"
},
{
"type": "string",
"optional": true,
"field": "gender_identity"
},
{
"type": "string",
"optional": true,
"field": "external_id"
},
{
"type": "int64",
"optional": true,
"field": "state_id"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.time.ZonedTimestamp",
"version": 1,
"field": "enrollment_date"
}
],
"optional": true,
"name": "dbserver1.public.member.Value",
"field": "after"
},
{
"type": "struct",
"fields": [
{
"type": "string",
"optional": false,
"field": "version"
},
{
"type": "string",
"optional": false,
"field": "connector"
},
{
"type": "string",
"optional": false,
"field": "name"
},
{
"type": "int64",
"optional": false,
"field": "ts_ms"
},
{
"type": "string",
"optional": true,
"name": "io.debezium.data.Enum",
"version": 1,
"parameters": {
"allowed": "true,last,false"
},
"default": "false",
"field": "snapshot"
},
{
"type": "string",
"optional": false,
"field": "db"
},
{
"type": "string",
"optional": false,
"field": "schema"
},
{
"type": "string",
"optional": false,
"field": "table"
},
{
"type": "int64",
"optional": true,
"field": "txId"
},
{
"type": "int64",
"optional": true,
"field": "lsn"
},
{
"type": "int64",
"optional": true,
"field": "xmin"
}
],
"optional": false,
"name": "io.debezium.connector.postgresql.Source",
"field": "source"
},
{
"type": "string",
"optional": false,
"field": "op"
},
{
"type": "int64",
"optional": true,
"field": "ts_ms"
}
],
"optional": false,
"name": "dbserver1.public.member.Envelope"
},
"payload": {
"before": {
"id": 2265,
"first_name": null,
"last_name": null,
"display_name": null,
"profile_picture": null,
"date_of_birth": null,
"email_address": "",
"is_deleted": false,
"vhm_legacy_id": 0,
"created_date": "1970-01-01T00:00:00Z",
"updated_date": null,
"gender": null,
"background_image": null,
"words_to_live_by": null,
"my_bio": null,
"sponsor_id": null,
"timezone_id": null,
"language": null,
"unit_of_measurement": null,
"business_unit_id": null,
"office_id": null,
"is_wizard_form_completed": null,
"is_internal": null,
"member_settings_id": 0,
"status": "",
"title": null,
"department": null,
"location": null,
"type": "",
"country": null,
"hire_date": null,
"original_hire_date": null,
"employee_id": null,
"division": null,
"failed_security_question_count": null,
"security_question_lockout": null,
"country_id": null,
"user_name": null,
"phone_number": null,
"gender_identity": null,
"external_id": null,
"state_id": null,
"enrollment_date": null
},
"after": null,
"source": {
"version": "1.0.2.Final",
"connector": "postgresql",
"name": "dbserver1",
"ts_ms": 1584553562165,
"snapshot": "false",
"db": "genesis",
"schema": "public",
"table": "member",
"txId": 608,
"lsn": 34202232,
"xmin": null
},
"op": "d",
"ts_ms": 1584553562168
}
}
```