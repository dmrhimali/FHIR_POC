package com.example.fhirserver

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.model.primitive.IdDt
import ca.uhn.fhir.rest.annotation.*
import ca.uhn.fhir.rest.api.MethodOutcome
import ca.uhn.fhir.rest.server.IResourceProvider
import com.example.fhirserver.repository.PatientDao
import com.example.fhirserver.repository.PatientService
import mapToFhirPatient
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MyPatientResourceProvider(
    @Autowired
    private val patientService: PatientService,
) : IResourceProvider {


    @Autowired
    var patientDao: PatientDao? = null


    override fun getResourceType(): Class<out IBaseResource?>? {
        return MyPatient::class.java
    }

    //GET http://localhost:8083/Patient/28848325519084246 (id is json document id)
    @Read
    fun read(@IdParam theId: IdType?): Patient? {
        println("Received Patient read request for id ${theId.toString()}")
        val patientId = theId.toString().split("/")[1]
        return patientDao?.findById(patientId)?.get()?.mapToFhirPatient()
    }


    //https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations.html
    //POST http://localhost:8083/Patient
    @Create
    fun createPatient(@ResourceParam patient: Patient) : MethodOutcome{
        val patientId = patientService.savePatient(patient)
        val methodOutcome = MethodOutcome()
        methodOutcome.created = true
        methodOutcome.id = IdDt(patientId.toLong())
        return methodOutcome
    }

    //GET http://localhost:8083/Patient
    @Search
    fun getAllPatients(): List<Resource?>? {
        return patientService?.getAllPatients()
    }

    //DELETE http://localhost:8083/Patient/-5497449227270477075 (id of json document)
    @Delete
    fun delete(@IdParam theId: IdType?) {
        val patientId = theId.toString().split("/")[1]
        println("Received Patient delete request for id $patientId")
        patientService?.deletePatient(patientId)
    }

}

