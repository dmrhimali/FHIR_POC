package com.example.fhirclient

import ca.uhn.fhir.context.FhirContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class FhirClientApplication {

    var logger: Logger = LoggerFactory.getLogger(this.javaClass)

    var ctx: FhirContext = FhirContext.forR4()


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FhirClientApplication::class.java, *args)
        }
    }
}