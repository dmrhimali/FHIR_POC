package com.example.kafkatofhiringestion

import com.fasterxml.jackson.databind.ObjectMapper
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.StringType
import java.io.Serializable
import java.util.*

data class KafkaMessage(
    var payload: Payload? = null,
    var schema: Schema? = null
): Serializable

data class Payload(
    var after: PostgreTableFields? = null,
    var before: PostgreTableFields? = null,
    var op: String? = null,
    var source: Source? = null,
    var ts_ms: Long? = null
): Serializable

data class Schema(
    var fields: List<Field>? = null,
    var name: String? = null,
    var optional: Boolean? = null,
    var type: String? = null
): Serializable


data class Field(
    var `field`: String? = null,
    var fields: List<FieldX>? = null,
    var name: String? = null,
    var optional: Boolean? = null,
    var type: String? = null
): Serializable

data class FieldX(
    var default: String? = null,
    var `field`: String? = null,
    var name: String? = null,
    var optional: Boolean? = null,
    var parameters: Parameters? = null,
    var type: String? = null,
    var version: Int? = null
): Serializable

data class Source(
    var connector: String? = null,
    var db: String? = null,
    var lsn: Int? = null,
    var name: String? = null,
    var schema: String? = null,
    var snapshot: String? = null,
    var table: String? = null,
    var ts_ms: Long? = null,
    var txId: Int? = null,
    var version: String? = null,
    var xmin: Any? = null
): Serializable

data class Parameters(
    var allowed: String? = null
): Serializable

//all fields in member table cdc'd into kafka topic
data class PostgreTableFields(
    var    id: Int? = null,
    var    status: String? = null,
    var    first_name: String? = null,
    var    last_name: String? = null,
    var    date_of_birth: Date? = null,
    var    gender: String? = null,
    var    address_line_1: String? = null,
    var    address_line_2: String? = null,
    var    address_line_3: String? = null,
    var    city: String? = null,
    var    state: String? = null,
    var    zipcode: String? = null,
    var    country: String? = null,
    var    sponsor_id: Int? = null,
): Serializable

fun mapFromJsonString(rawJsonString: String): KafkaMessage {
    val mapper = ObjectMapper()
    return mapper.readValue(rawJsonString, KafkaMessage::class.java)
}

fun KafkaMessage.getPatient(): Patient {
    val patient = Patient()

    this.payload?.after?.let { tableFields ->
        tableFields.id?.let {
            //patient.id = "Patient/$it" //This does not work as fhir generate its own UUID value
            patient.addIdentifier().setSystem("VP").setValue("$it")
        }

        tableFields.first_name?.let { patient.addName().addGiven(it) }
        tableFields.last_name?.let { patient.addName().family = it }
        tableFields.status?.let { patient.active = it.equals("active", true) }
        tableFields.address_line_1.let { patient.addAddress().line.add(StringType(it)) }
        tableFields.address_line_2.let { patient.addAddress().line.add(StringType(it)) }
        tableFields.address_line_3.let { patient.addAddress().line.add(StringType(it)) }
        tableFields.city.let { patient.addAddress().city = it }
        tableFields.state.let { patient.addAddress().state = it }
        tableFields.zipcode.let { patient.addAddress().postalCode = it }
        tableFields.date_of_birth.let { patient.birthDate = it }
        tableFields.gender?.let { patient.gender = Enumerations.AdministrativeGender.valueOf(it.uppercase()) }
    }

    if ( this.payload?.after == null) { //happens if it is a delete record
        this.payload?.before?.let { tableFields ->
            tableFields.id?.let {
                //patient.id = "Patient/$it" //This does not work as fhir generate its own UUID value
                patient.addIdentifier().setSystem("VP").setValue("$it")
            }

            tableFields.first_name?.let { patient.addName().addGiven(it) }
            tableFields.last_name?.let { patient.addName().family = it }
            tableFields.status?.let { patient.active = it.equals("active", true) }
            tableFields.address_line_1.let { patient.addAddress().line.add(StringType(it)) }
            tableFields.address_line_2.let { patient.addAddress().line.add(StringType(it)) }
            tableFields.address_line_3.let { patient.addAddress().line.add(StringType(it)) }
            tableFields.city.let { patient.addAddress().city = it }
            tableFields.state.let { patient.addAddress().state = it }
            tableFields.zipcode.let { patient.addAddress().postalCode = it }
            tableFields.date_of_birth.let { patient.birthDate = it }
            tableFields.gender?.let { patient.gender = Enumerations.AdministrativeGender.valueOf(it.uppercase()) }
        }
    }
    return  patient
}