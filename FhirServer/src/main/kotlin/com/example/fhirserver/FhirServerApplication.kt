package com.example.fhirserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
class FhirServerApplication {

    @Autowired
    private val context: ApplicationContext? = null

    @Bean
    fun servletRegistrationBean(): ServletRegistrationBean<*> {
        val registration =  ServletRegistrationBean(context?.let { FhirRestfulServer(it) }, "/*")
        registration.setName("FhirServlet")
        return registration
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FhirServerApplication::class.java, *args)
        }
    }
}