package com.example.fhirclient

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Controller
class GenesisFhirRestController {

    var client: IGenericClient? = null
    var ctx: FhirContext? = null
    var parser: IParser? = null
    init {
        ctx = FhirContext.forR4()
        ctx?.restfulClientFactory?.socketTimeout = 200 * 1000
        client = ctx?.newRestfulGenericClient(FHIR_SERVER_BASE_URL)
        parser = ctx!!.newJsonParser().setPrettyPrint(true)

    }

    //https://hapifhir.io/hapi-fhir/docs/client/generic_client.html
    //https://hapifhir.io/hapi-fhir/docs/model/parsers.html
    @PostMapping("/syncPatients")
    fun syncPatient(): ResponseEntity<String> {
        val patient = Patient()
        patient.addName().setFamily("Tom").addGiven("Smith")
        patient.addIdentifier().setSystem("http://virginpulse/Identifiers").value = "09832345234543876873"

        val outcome = client!!.create()
            .resource(patient)
            .prettyPrint()
            .encodedJson()
            .execute()

        val id: IIdType = outcome.id

        println("Got ID: " + id.value)

        return ResponseEntity.ok().body("completed")
    }

    // see Multi-valued Parameters (ANY/OR) (ALL/AND) https://hapifhir.io/hapi-fhir/docs/client/generic_client.html#search
    @RequestMapping(path = ["/getByGivenName"])
    fun searchByFamilyName(@RequestParam familyName: String?): ResponseEntity<String> {
        val response = client!!.search<IBaseBundle>()
            .forResource(Patient::class.java)
            .where(Patient.FAMILY.matches().values(familyName))
            .returnBundle(Bundle::class.java)
            .execute()


        val serialized = parser?.encodeResourceToString(response)
        return ResponseEntity.ok().body(serialized)
    }

    //https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations_search.html
    //https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations_search.html#multi-valued-andor-parameters
    //OR: ?language=FR,NL, AND: ?language=FR&language=NL
    //It is worth noting that according to the FHIR specification,
    // you can have an AND relationship combining multiple OR relationships, but not vice-versa.
    // In other words, it's possible to support a search like ("name" = ("joe" or "john")) AND ("age" = (11 or 12))
    // but not a search like ("language" = ("en" AND "fr") OR ("address" = ("Canada" AND "Quebec")).
    // If you wish to support the latter, you may consider implementing the _filter parameter.
    @RequestMapping(path = ["/searchPatientByUrl"])
    fun searchPatientByUrl(@RequestBody payload: Map<String?, Any?>?): ResponseEntity<String> {
        var searchUrl = FHIR_SERVER_BASE_URL
        if (!payload.isNullOrEmpty()) {
            val params = payload?.map { (k, v) ->
                "$k=$v"
            }?.joinToString("&")
            searchUrl = "$searchUrl/Patient?$params"
        }

        println("searchUrl = $searchUrl")

        var response = client!!.search<IBaseBundle>()
            .byUrl(searchUrl)
            .returnBundle(Bundle::class.java)
            .execute()

        val serialized = parser?.encodeResourceToString(response)
        return ResponseEntity.ok().body(serialized)
    }

    companion object {
       const val FHIR_SERVER_BASE_URL = "http://localhost:8080/fhir"
    }
}