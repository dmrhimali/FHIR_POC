We will create a generic client that will use rest api in fhir server.

## 1. spin up hapi-fhir-jpaserver-starter.

## 2. import dependencies to pm.xml

_```xml
        <dependency>
            <groupId>javax.ws.rs</groupId>
            <artifactId>javax.ws.rs-api</artifactId>
            <version>2.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--start: hapi fhir dependencies -->
        <dependency>
            <groupId>ca.uhn.hapi.fhir</groupId>
            <artifactId>hapi-fhir-structures-r4</artifactId>
            <version>5.2.0</version>
        </dependency>
        <dependency>
            <groupId>ca.uhn.hapi.fhir</groupId>
            <artifactId>hapi-fhir-base</artifactId>
            <version>5.2.0</version>
        </dependency>
        <dependency>
            <groupId>ca.uhn.hapi.fhir</groupId>
            <artifactId>hapi-fhir-server</artifactId>
            <version>5.2.0</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/ca.uhn.hapi.fhir/hapi-fhir-client -->
        <dependency>
            <groupId>ca.uhn.hapi.fhir</groupId>
            <artifactId>hapi-fhir-client</artifactId>
            <version>5.2.0</version>
        </dependency>
     
```_

## SpringboootApplication:

```kotlin
@SpringBootApplication
class KafkaToFhirIngestionApplication {

    var logger: Logger = LoggerFactory.getLogger(this.javaClass)

    var ctx: FhirContext = FhirContext.forR4()


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(KafkaToFhirIngestionApplication::class.java, *args)
        }
    }
}
```

## Write GenesisFhirRestController

```kotlin
package com.example.kafkatofhiringestion

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


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



    companion object {
       const val FHIR_SERVER_BASE_URL = "http://localhost:8080/fhir"
    }
}
```

## Run the program

```shell
/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/bin/java -XX:TieredStopAtLevel=1 -noverify -Dspring.output.ansi.enabled=always -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=52897:/Applications/IntelliJ IDEA.app/Contents/bin -Dcom.sun.management.jmxremote -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/tools.jar:/Users/rasanjalee.dissanaya/home/code/FHIR_POC/KafkaToFhirIngestion/target/classes:/Users/rasanjalee.dissanaya/.m2/repository/javax/ws/rs/javax.ws.rs-api/2.0/javax.ws.rs-api-2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-actuator/2.5.5/spring-boot-starter-actuator-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter/2.5.5/spring-boot-starter-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot/2.5.5/spring-boot-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.5.5/spring-boot-autoconfigure-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-logging/2.5.5/spring-boot-starter-logging-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/ch/qos/logback/logback-classic/1.2.6/logback-classic-1.2.6.jar:/Users/rasanjalee.dissanaya/.m2/repository/ch/qos/logback/logback-core/1.2.6/logback-core-1.2.6.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.14.1/log4j-to-slf4j-2.14.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/logging/log4j/log4j-api/2.14.1/log4j-api-2.14.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/slf4j/jul-to-slf4j/1.7.32/jul-to-slf4j-1.7.32.jar:/Users/rasanjalee.dissanaya/.m2/repository/jakarta/annotation/jakarta.annotation-api/1.3.5/jakarta.annotation-api-1.3.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/yaml/snakeyaml/1.28/snakeyaml-1.28.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-actuator-autoconfigure/2.5.5/spring-boot-actuator-autoconfigure-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-actuator/2.5.5/spring-boot-actuator-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.12.5/jackson-datatype-jsr310-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/io/micrometer/micrometer-core/1.7.4/micrometer-core-1.7.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/hdrhistogram/HdrHistogram/2.1.12/HdrHistogram-2.1.12.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/latencyutils/LatencyUtils/2.0.3/LatencyUtils-2.0.3.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-web/2.5.5/spring-boot-starter-web-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-json/2.5.5/spring-boot-starter-json-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.12.5/jackson-datatype-jdk8-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/module/jackson-module-parameter-names/2.12.5/jackson-module-parameter-names-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-tomcat/2.5.5/spring-boot-starter-tomcat-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.53/tomcat-embed-core-9.0.53.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.53/tomcat-embed-el-9.0.53.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/9.0.53/tomcat-embed-websocket-9.0.53.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-web/5.3.10/spring-web-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-beans/5.3.10/spring-beans-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-webmvc/5.3.10/spring-webmvc-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-aop/5.3.10/spring-aop-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-expression/5.3.10/spring-expression-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-structures-r4/5.2.0/hapi-fhir-structures-r4-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/org.hl7.fhir.utilities/5.2.0/org.hl7.fhir.utilities-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/org.hl7.fhir.r4/5.2.0/org.hl7.fhir.r4-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-base/5.2.0/hapi-fhir-base-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.12.5/jackson-databind-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.12.5/jackson-core-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/commons/commons-text/1.7/commons-text-1.7.jar:/Users/rasanjalee.dissanaya/.m2/repository/commons-codec/commons-codec/1.15/commons-codec-1.15.jar:/Users/rasanjalee.dissanaya/.m2/repository/commons-io/commons-io/2.6/commons-io-2.6.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/guava/guava/28.2-android/guava-28.2-android.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/slf4j/slf4j-api/1.7.32/slf4j-api-1.7.32.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/slf4j/jcl-over-slf4j/1.7.32/jcl-over-slf4j-1.7.32.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-server/5.2.0/hapi-fhir-server-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/httpcomponents/httpcore/4.4.14/httpcore-4.4.14.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/commons/commons-collections4/4.3/commons-collections4-4.3.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-messaging/5.3.10/spring-messaging-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-client/5.2.0/hapi-fhir-client-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/cloud/spring-cloud-stream/3.0.3.RELEASE/spring-cloud-stream-3.0.3.RELEASE.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-validation/2.5.5/spring-boot-starter-validation-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/hibernate/validator/hibernate-validator/6.2.0.Final/hibernate-validator-6.2.0.Final.jar:/Users/rasanjalee.dissanaya/.m2/repository/jakarta/validation/jakarta.validation-api/2.0.2/jakarta.validation-api-2.0.2.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jboss/logging/jboss-logging/3.4.2.Final/jboss-logging-3.4.2.Final.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/classmate/1.5.1/classmate-1.5.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/integration/spring-integration-core/5.5.4/spring-integration-core-5.5.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/io/projectreactor/reactor-core/3.4.10/reactor-core-3.4.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/integration/spring-integration-jmx/5.5.4/spring-integration-jmx-5.5.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/retry/spring-retry/1.3.1/spring-retry-1.3.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/cloud/spring-cloud-function-context/3.0.3.RELEASE/spring-cloud-function-context-3.0.3.RELEASE.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/cloud/spring-cloud-function-core/3.0.3.RELEASE/spring-cloud-function-core-3.0.3.RELEASE.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/cloud/spring-cloud-stream-binder-kafka/3.0.3.RELEASE/spring-cloud-stream-binder-kafka-3.0.3.RELEASE.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/cloud/spring-cloud-stream-binder-kafka-core/3.0.3.RELEASE/spring-cloud-stream-binder-kafka-core-3.0.3.RELEASE.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/integration/spring-integration-kafka/5.5.4/spring-integration-kafka-5.5.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/kafka/kafka-clients/2.7.1/kafka-clients-2.7.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/github/luben/zstd-jni/1.4.5-6/zstd-jni-1.4.5-6.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/lz4/lz4-java/1.7.1/lz4-java-1.7.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/xerial/snappy/snappy-java/1.1.7.7/snappy-java-1.1.7.7.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/kafka/spring-kafka/2.7.7/spring-kafka-2.7.7.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-context/5.3.10/spring-context-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-tx/5.3.10/spring-tx-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/cloud/spring-cloud-stream-binder-kafka-streams/3.0.3.RELEASE/spring-cloud-stream-binder-kafka-streams-3.0.3.RELEASE.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/kafka/kafka-streams/2.7.1/kafka-streams-2.7.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/kafka/connect-json/2.7.1/connect-json-2.7.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/kafka/connect-api/2.7.1/connect-api-2.7.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/rocksdb/rocksdbjni/5.18.4/rocksdbjni-5.18.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/module/jackson-module-kotlin/2.12.5/jackson-module-kotlin-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.5/jackson-annotations-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-reflect/1.5.31/kotlin-reflect-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.5.31/kotlin-stdlib-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-common/1.5.31/kotlin-stdlib-common-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.5.31/kotlin-stdlib-jdk8-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.5.31/kotlin-stdlib-jdk7-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-core/5.3.10/spring-core-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-jcl/5.3.10/spring-jcl-5.3.10.jar com.example.kafkatofhiringestion.KafkaToFhirIngestionApplication

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-07 13:03:21.503  INFO 66152 --- [           main] afkaToFhirIngestionApplication$Companion : Starting KafkaToFhirIngestionApplication.Companion using Java 1.8.0_292 on VP2461 with PID 66152 (/Users/rasanjalee.dissanaya/home/code/FHIR_POC/KafkaToFhirIngestion/target/classes started by rasanjalee.dissanaya in /Users/rasanjalee.dissanaya/home/code/FHIR_POC)
2021-10-07 13:03:21.505  INFO 66152 --- [           main] afkaToFhirIngestionApplication$Companion : No active profile set, falling back to default profiles: default
2021-10-07 13:03:22.367  INFO 66152 --- [           main] faultConfiguringBeanFactoryPostProcessor : No bean named 'errorChannel' has been explicitly defined. Therefore, a default PublishSubscribeChannel will be created.
2021-10-07 13:03:22.375  INFO 66152 --- [           main] faultConfiguringBeanFactoryPostProcessor : No bean named 'integrationHeaderChannelRegistry' has been explicitly defined. Therefore, a default DefaultHeaderChannelRegistry will be created.
2021-10-07 13:03:22.433  INFO 66152 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.integration.config.IntegrationManagementConfiguration' of type [org.springframework.integration.config.IntegrationManagementConfiguration] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2021-10-07 13:03:22.435  INFO 66152 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration$IntegrationJmxConfiguration' of type [org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration$IntegrationJmxConfiguration] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2021-10-07 13:03:22.443  INFO 66152 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration' of type [org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2021-10-07 13:03:22.447  INFO 66152 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'mbeanServer' of type [com.sun.jmx.mbeanserver.JmxMBeanServer] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2021-10-07 13:03:22.460  INFO 66152 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'integrationChannelResolver' of type [org.springframework.integration.support.channel.BeanFactoryChannelResolver] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2021-10-07 13:03:22.461  INFO 66152 --- [           main] trationDelegate$BeanPostProcessorChecker : Bean 'integrationDisposableAutoCreatedBeans' of type [org.springframework.integration.config.annotation.Disposables] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)
2021-10-07 13:03:22.712  INFO 66152 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8082 (http)
2021-10-07 13:03:22.720  INFO 66152 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-07 13:03:22.720  INFO 66152 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
2021-10-07 13:03:22.798  INFO 66152 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-10-07 13:03:22.799  INFO 66152 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1258 ms
2021-10-07 13:03:22.939  INFO 66152 --- [           main] ca.uhn.fhir.util.VersionUtil             : HAPI FHIR version 5.2.0 - Rev 50b927b7d2
2021-10-07 13:03:22.941  INFO 66152 --- [           main] ca.uhn.fhir.context.FhirContext          : Creating new FHIR context for FHIR version [R4]
2021-10-07 13:03:22.946  INFO 66152 --- [           main] ca.uhn.fhir.context.FhirContext          : Creating new FHIR context for FHIR version [R4]
2021-10-07 13:03:23.076  INFO 66152 --- [           main] ca.uhn.fhir.util.XmlUtil                 : FHIR XML procesing will use StAX implementation 'Java Runtime Environment' version '1.8.0_292'
2021-10-07 13:03:23.351  INFO 66152 --- [           main] c.f.c.c.BeanFactoryAwareFunctionRegistry : Looking up function 'null' with acceptedOutputTypes: []
2021-10-07 13:03:23.569  INFO 66152 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
2021-10-07 13:03:23.662  INFO 66152 --- [           main] o.s.i.monitor.IntegrationMBeanExporter   : Registering MessageChannel errorChannel
2021-10-07 13:03:23.702  INFO 66152 --- [           main] o.s.i.monitor.IntegrationMBeanExporter   : Registering MessageChannel nullChannel
2021-10-07 13:03:23.709  INFO 66152 --- [           main] o.s.i.monitor.IntegrationMBeanExporter   : Registering MessageHandler _org.springframework.integration.errorLogger
2021-10-07 13:03:23.740  INFO 66152 --- [           main] o.s.i.endpoint.EventDrivenConsumer       : Adding {logging-channel-adapter:_org.springframework.integration.errorLogger} as a subscriber to the 'errorChannel' channel
2021-10-07 13:03:23.740  INFO 66152 --- [           main] o.s.i.channel.PublishSubscribeChannel    : Channel 'application.errorChannel' has 1 subscriber(s).
2021-10-07 13:03:23.741  INFO 66152 --- [           main] o.s.i.endpoint.EventDrivenConsumer       : started bean '_org.springframework.integration.errorLogger'
2021-10-07 13:03:23.760  INFO 66152 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8082 (http) with context path ''
2021-10-07 13:03:23.788  INFO 66152 --- [           main] afkaToFhirIngestionApplication$Companion : Started KafkaToFhirIngestionApplication.Companion in 2.592 seconds (JVM running for 3.107)
2021-10-07 13:03:24.277  INFO 66152 --- [-192.168.86.246] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2021-10-07 13:03:24.277  INFO 66152 --- [-192.168.86.246] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2021-10-07 13:03:24.278  INFO 66152 --- [-192.168.86.246] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
```
## Call Api

**POST http://localhost:8082/syncPatients**

ouptput:
```shell
completed
```

**GET http://localhost:8082/getByGivenName?familyName=Tom**

output:
```json
{
  "resourceType": "Bundle",
  "id": "7c6ed8fa-0c4c-4542-b6c0-93f4436afa51",
  "meta": {
    "lastUpdated": "2021-10-07T18:05:06.164+00:00"
  },
  "type": "searchset",
  "total": 4,
  "link": [ {
    "relation": "self",
    "url": "http://localhost:8080/fhir/Patient?family=Tom"
  } ],
  "entry": [ {
    "fullUrl": "http://localhost:8080/fhir/Patient/54",
    "resource": {
      "resourceType": "Patient",
      "id": "54",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T16:44:41.228+00:00",
        "source": "#sXX0rV7mHqYUF9yX"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/55",
    "resource": {
      "resourceType": "Patient",
      "id": "55",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T16:54:27.948+00:00",
        "source": "#qY8bN7CtCs04Q41T"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/56",
    "resource": {
      "resourceType": "Patient",
      "id": "56",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T17:36:00.846+00:00",
        "source": "#pYt0iiBkDBfHiQS8"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/57",
    "resource": {
      "resourceType": "Patient",
      "id": "57",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T17:38:18.206+00:00",
        "source": "#NN3xZ7S8V7C8BYCO"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  } ]
}
```


## search by url

```kotlin
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

```

**GET http://localhost:8082/searchPatientByUrl** with json/body:

```json
{
    "given" : "smith",
    "family" : "tom"
}
```

output:
```json
{
  "resourceType": "Bundle",
  "id": "6fe7970a-957e-414c-820d-6c80a500cb66",
  "meta": {
    "lastUpdated": "2021-10-07T19:08:38.454+00:00"
  },
  "type": "searchset",
  "total": 5,
  "link": [ {
    "relation": "self",
    "url": "http://localhost:8080/fhir/Patient?family=tom&given=smith"
  } ],
  "entry": [ {
    "fullUrl": "http://localhost:8080/fhir/Patient/54",
    "resource": {
      "resourceType": "Patient",
      "id": "54",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T16:44:41.228+00:00",
        "source": "#sXX0rV7mHqYUF9yX"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/55",
    "resource": {
      "resourceType": "Patient",
      "id": "55",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T16:54:27.948+00:00",
        "source": "#qY8bN7CtCs04Q41T"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/56",
    "resource": {
      "resourceType": "Patient",
      "id": "56",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T17:36:00.846+00:00",
        "source": "#pYt0iiBkDBfHiQS8"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/57",
    "resource": {
      "resourceType": "Patient",
      "id": "57",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T17:38:18.206+00:00",
        "source": "#NN3xZ7S8V7C8BYCO"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  }, {
    "fullUrl": "http://localhost:8080/fhir/Patient/58",
    "resource": {
      "resourceType": "Patient",
      "id": "58",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2021-10-07T18:13:24.703+00:00",
        "source": "#dUkX2r3EMMEQpPGw"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Smith <b>TOM </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>09832345234543876873</td></tr></tbody></table></div>"
      },
      "identifier": [ {
        "system": "http://virginpulse/Identifiers",
        "value": "09832345234543876873"
      } ],
      "name": [ {
        "family": "Tom",
        "given": [ "Smith" ]
      } ]
    },
    "search": {
      "mode": "match"
    }
  } ]
}
```

Note based on code comments above: ANDing of ORs possible but not viz.versa. You need filters if you do.

## Custom Resources


