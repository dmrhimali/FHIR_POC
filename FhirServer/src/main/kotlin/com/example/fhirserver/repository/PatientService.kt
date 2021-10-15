package com.example.fhirserver.repository

import VPPatient
import ca.uhn.fhir.rest.annotation.IdParam
import mapFromFhirPatient
import mapToFhirPatient
import org.hl7.fhir.r4.model.Patient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class PatientService(
        @Autowired
        val patientDao: PatientDao
) {

    fun getPatientById( id: String?) : Patient? {
        return id?.let { patientDao.findById(it)?.get()?.mapToFhirPatient() }
    }

    fun getPatientByGender(gender: String?) : List<Patient?>? {
        return patientDao.findByGender(gender)?.map {  vppatient ->
            vppatient?.mapToFhirPatient()
        }
    }

    fun getAllPatients(): List<Patient?>?  {
        return patientDao.findAll().map {  vppatient ->
            vppatient?.mapToFhirPatient()
        }
    }

    fun savePatient( patient: Patient) : String{
        println("patient = ${patient}")
        val patientId =  Random.nextLong().toString()
        patient.id = patientId
        val vpPatient = VPPatient(id = patientId, resourceType = "Patient").mapFromFhirPatient(patient)
        println("vpPatient = $vpPatient")
        patientDao.save(vpPatient)

        return patientId
    }

    fun deletePatient(id: String) {
        patientDao.deleteById(id)
    }
}