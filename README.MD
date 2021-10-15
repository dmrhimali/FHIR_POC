## FHIR POC

This project contain few subprojects that demo impmentation/use of fhir using **hapi fhir**:

## 1. FhirServer
Plian fhir server implementation using elasticsearch as backend : Here we bring in persistence layer , fhir brings rest api.

(This is an alternative to jpa-fhir-starter offered by hapi fhir): This prject brings in both persistence layer+ rest api

## 2. FhirClient
A fhir client to query and peprform crud operations on fhirserver

## 3. KafkaToFhir
Ingestion framework that read cdc data from a postgres table (using debezium) and ingest to fhir backend

See **KafkaToFhirIngestion/README.MD** on details on how to install kafka locally had use debezium to cdc records.

KafkaToFhirIngestion/README.MD also has a guid on how to setup project and run.