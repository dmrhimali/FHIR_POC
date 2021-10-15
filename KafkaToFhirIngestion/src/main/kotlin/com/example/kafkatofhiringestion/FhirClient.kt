package com.example.kafkatofhiringestion

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.annotation.ResourceParam
import ca.uhn.fhir.rest.api.MethodOutcome
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Patient
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Component
class FhirClient {

    var client: IGenericClient? = null
    var ctx: FhirContext? = null
    var parser: IParser? = null

    init {
        ctx = FhirContext.forR4()
        ctx?.restfulClientFactory?.socketTimeout = 200 * 1000
        client = ctx?.newRestfulGenericClient(FHIR_SERVER_BASE_URL)
        parser = ctx!!.newJsonParser().setPrettyPrint(true)

    }

    fun createPatient(@ResourceParam patient: Patient): String {
        val outcome = client!!.create()
            .resource(patient)
            .prettyPrint()
            .encodedJson()
            .execute()

        val id: IIdType = outcome.id

       return  id.value

    }

    fun updatePatient(@ResourceParam patient: Patient, @ResourceParam vpId: String): String {
//        val outcome: MethodOutcome = client!!.update()
//            .resource(patient)
//            .execute()

        val outcome: MethodOutcome = client!!
            .update()
            .resource(patient)
            .conditional()
            .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("VP", vpId))
            .execute();

        val id: IIdType = outcome.id

        return id.value

    }


    fun deletePatient(@ResourceParam vpId: String): String? {
//        val response = client!!
//            .delete()
//            .resourceById(IdType("Patient", id))
//            .execute()

        val response = client!!
            .delete()
            .resourceConditionalByType("Patient")
            .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("VP", vpId))
            .execute();

        val outcome: OperationOutcome? = response.operationOutcome as OperationOutcome

        println(outcome?.issueFirstRep?.details?.codingFirstRep?.code)

        return outcome?.issueFirstRep?.toString()
    }


    companion object {
       const val FHIR_SERVER_BASE_URL = "http://localhost:8080/fhir"
    }
}