package com.example.fhirserver.repository

import VPPatient
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientDao: ElasticsearchRepository<VPPatient, String> {
    fun findByGender(gender: String?): List<VPPatient?>?
}
