package com.example.fhirserver


import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.server.IResourceProvider
import ca.uhn.fhir.rest.server.RestfulServer
import com.example.fhirserver.repository.PatientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.server.adapter.WebHttpHandlerBuilder.applicationContext
import java.util.*
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet


@Component
@WebServlet("/*")
class FhirRestfulServer (
    private val applicationContext: ApplicationContext
        ): RestfulServer(){

    @Throws(ServletException::class)
    override fun initialize() {
        super.initialize()
        fhirContext = FhirContext.forR4()
        resourceProviders = mutableListOf(applicationContext?.getBean(PatientResourceProvider::class.java)) as Collection<IResourceProvider>?

    }
}

