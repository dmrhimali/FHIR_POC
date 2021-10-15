Reference: https://ordina-jworks.github.io/ehealth/2021/02/23/hapi-fhir.html

## Fhir

#TODO


**Mongo backend and @OptionalParam:**
https://ordina-jworks.github.io/ehealth/2021/02/23/hapi-fhir.html


**How to save patients in backend**

https://medium.com/@yashdsaraf/gsoc-librehealth-first-week-of-coding-period-bc72aa0613c5

code: 

https://gitlab.com/yashdsaraf/hapi-fhir-spring-boot-demo/-/tree/master/src/main/java/com/example/demo



## 1. BRING UP SERVER

### add hapi fhir and postgresql dependencies to FhirServer pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>FHIR_POC</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>FhirServer</module>
    </modules>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <java.version>1.8</java.version>
        <kotlin.version>1.3.72</kotlin.version>
        <kotlin.compiler.jvmTarget>1.8</kotlin.compiler.jvmTarget>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib-jdk8</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib-jdk7</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-test</artifactId>
            <version>${kotlin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-reflect</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.11.1</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.5</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.1.1</version>
                <executions>
                    <execution>
                        <id>unpack-newrelic</id>
                        <phase>package</phase>
                        <goals>
                            <goal>unpack-dependencies</goal>
                        </goals>
                        <configuration>
                            <includeGroupIds>com.newrelic.agent.java</includeGroupIds>
                            <includeArtifactIds>newrelic-java</includeArtifactIds>
                            <!-- you can optionally exclude files -->
                            <!-- <excludes>**/newrelic.yml</excludes> -->
                            <overWriteReleases>false</overWriteReleases>
                            <overWriteSnapshots>false</overWriteSnapshots>
                            <overWriteIfNewer>true</overWriteIfNewer>
                            <outputDirectory>${project.build.directory}</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>process-sources</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <source>src/main/java</source>
                                <source>src/main/kotlin</source>
                                <source>src/main/resources</source>
                            </sourceDirs>
                        </configuration>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>process-test-sources</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                        <configuration>
                            <sourceDirs>
                                <source>src/test/java</source>
                                <source>src/test/kotlin</source>
                                <source>src/test/resources</source>
                            </sourceDirs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Create fhir restful server i FhirServer

Note that we are using Release 4 (R4) of the FHIR specification. 
This is where you configure most of your HAPI server settings

**FhirRestfulServer.kt**
```kotlin
package com.example.fhirserver


import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.server.RestfulServer
import org.springframework.context.ApplicationContext
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet

@WebServlet("/*")
class FhirRestfulServer (context: ApplicationContext): RestfulServer(){
    @Throws(ServletException::class)
    override fun initialize() {
        super.initialize()
        fhirContext = FhirContext.forR4()
    }
}

```

### Register fhir context in FhirServer

**FhirServerApplication.kt**
```kotlin
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
        val registration =  ServletRegistrationBean(FhirRestfulServer(context!!), "/*")
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
```

### Bring up fhir server
By this point you should be able to run FhirServer project :

```shell
/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/bin/java -XX:TieredStopAtLevel=1 -noverify -Dspring.output.ansi.enabled=always -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=50412:/Applications/IntelliJ IDEA.app/Contents/bin -Dcom.sun.management.jmxremote -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/lib/tools.jar:/Users/rasanjalee.dissanaya/home/code/FHIR_POC/FhirServer/target/classes:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-web/2.5.5/spring-boot-starter-web-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-json/2.5.5/spring-boot-starter-json-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.12.5/jackson-datatype-jdk8-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.12.5/jackson-datatype-jsr310-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/module/jackson-module-parameter-names/2.12.5/jackson-module-parameter-names-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-tomcat/2.5.5/spring-boot-starter-tomcat-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.53/tomcat-embed-core-9.0.53.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.53/tomcat-embed-el-9.0.53.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/9.0.53/tomcat-embed-websocket-9.0.53.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-web/5.3.10/spring-web-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-beans/5.3.10/spring-beans-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-webmvc/5.3.10/spring-webmvc-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-aop/5.3.10/spring-aop-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-context/5.3.10/spring-context-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-expression/5.3.10/spring-expression-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-structures-r4/5.2.0/hapi-fhir-structures-r4-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/org.hl7.fhir.utilities/5.2.0/org.hl7.fhir.utilities-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/org.hl7.fhir.r4/5.2.0/org.hl7.fhir.r4-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-base/5.2.0/hapi-fhir-base-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.12.5/jackson-databind-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.5/jackson-annotations-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.12.5/jackson-core-2.12.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/commons/commons-text/1.7/commons-text-1.7.jar:/Users/rasanjalee.dissanaya/.m2/repository/commons-codec/commons-codec/1.15/commons-codec-1.15.jar:/Users/rasanjalee.dissanaya/.m2/repository/commons-io/commons-io/2.6/commons-io-2.6.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/guava/guava/29.0-jre/guava-29.0-jre.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:/Users/rasanjalee.dissanaya/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/slf4j/slf4j-api/1.7.32/slf4j-api-1.7.32.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/slf4j/jcl-over-slf4j/1.7.32/jcl-over-slf4j-1.7.32.jar:/Users/rasanjalee.dissanaya/.m2/repository/ca/uhn/hapi/fhir/hapi-fhir-server/5.2.0/hapi-fhir-server-5.2.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/httpcomponents/httpcore/4.4.14/httpcore-4.4.14.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/commons/commons-collections4/4.3/commons-collections4-4.3.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-messaging/5.3.10/spring-messaging-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/postgresql/postgresql/42.2.23/postgresql-42.2.23.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/checkerframework/checker-qual/3.5.0/checker-qual-3.5.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter/2.5.5/spring-boot-starter-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot/2.5.5/spring-boot-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.5.5/spring-boot-autoconfigure-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/boot/spring-boot-starter-logging/2.5.5/spring-boot-starter-logging-2.5.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/ch/qos/logback/logback-classic/1.2.6/logback-classic-1.2.6.jar:/Users/rasanjalee.dissanaya/.m2/repository/ch/qos/logback/logback-core/1.2.6/logback-core-1.2.6.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.14.1/log4j-to-slf4j-2.14.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/apache/logging/log4j/log4j-api/2.14.1/log4j-api-2.14.1.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/slf4j/jul-to-slf4j/1.7.32/jul-to-slf4j-1.7.32.jar:/Users/rasanjalee.dissanaya/.m2/repository/jakarta/annotation/jakarta.annotation-api/1.3.5/jakarta.annotation-api-1.3.5.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-core/5.3.10/spring-core-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/springframework/spring-jcl/5.3.10/spring-jcl-5.3.10.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/yaml/snakeyaml/1.28/snakeyaml-1.28.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-reflect/1.5.31/kotlin-reflect-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.5.31/kotlin-stdlib-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-common/1.5.31/kotlin-stdlib-common-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.5.31/kotlin-stdlib-jdk8-1.5.31.jar:/Users/rasanjalee.dissanaya/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.5.31/kotlin-stdlib-jdk7-1.5.31.jar com.example.fhirserver.FhirServerApplication

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-01 10:43:21.823  INFO 98884 --- [           main] c.e.f.FhirServerApplication$Companion    : Starting FhirServerApplication.Companion using Java 1.8.0_292 on VP2461 with PID 98884 (/Users/rasanjalee.dissanaya/home/code/FHIR_POC/FhirServer/target/classes started by rasanjalee.dissanaya in /Users/rasanjalee.dissanaya/home/code/FHIR_POC)
2021-10-01 10:43:21.825  INFO 98884 --- [           main] c.e.f.FhirServerApplication$Companion    : No active profile set, falling back to default profiles: default
2021-10-01 10:43:22.741  INFO 98884 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-10-01 10:43:22.746  INFO 98884 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-01 10:43:22.746  INFO 98884 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
2021-10-01 10:43:22.787  INFO 98884 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-10-01 10:43:22.788  INFO 98884 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 925 ms
2021-10-01 10:43:22.813  INFO 98884 --- [           main] ca.uhn.fhir.util.VersionUtil             : HAPI FHIR version 5.2.0 - Rev 50b927b7d2
2021-10-01 10:43:23.077  INFO 98884 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-10-01 10:43:23.086  INFO 98884 --- [           main] c.e.f.FhirServerApplication$Companion    : Started FhirServerApplication.Companion in 1.628 seconds (JVM running for 2.311)
2021-10-01 10:44:26.474  INFO 98884 --- [nio-8080-exec-2] ca.uhn.fhir.context.FhirContext          : Creating new FHIR context for FHIR version [R4]
2021-10-01 10:44:26.475  INFO 98884 --- [nio-8080-exec-2] ca.uhn.fhir.rest.server.RestfulServer    : Initializing HAPI FHIR restful server running in R4 mode
2021-10-01 10:44:26.851  INFO 98884 --- [nio-8080-exec-2] ca.uhn.fhir.rest.server.RestfulServer    : A FHIR has been lit on this server

```

### Call GET metadata endpoint
and hit endpoint GET **http://localhost:8080/metadata** :

A capability statement will appear as shown below. 
This isa set of capabilities (behaviors) of a FHIR Server for a particular version of FHIR that may be used as a statement of actual server functionality or a statement of required or desired server implementation. 
Congratulations you are now FHIR enabled!



output:

```json
{
    "resourceType": "CapabilityStatement",
    "status": "active",
    "date": "2021-10-01T10:44:26-05:00",
    "publisher": "Not provided",
    "kind": "instance",
    "software": {
        "name": "HAPI FHIR Server",
        "version": "5.2.0"
    },
    "implementation": {
        "description": "HAPI FHIR",
        "url": "http://localhost:8080/"
    },
    "fhirVersion": "4.0.1",
    "format": [
        "application/fhir+xml",
        "application/fhir+json"
    ],
    "rest": [
        {
            "mode": "server",
            "resource": [
                {
                    "type": "OperationDefinition",
                    "profile": "http://hl7.org/fhir/StructureDefinition/OperationDefinition",
                    "interaction": [
                        {
                            "code": "read"
                        }
                    ]
                }
            ]
        }
    ]
}
```

## CREATE RESOURCE

The PatientProvider is where the FHIR Patient behaviour is configured. 
HAPI uses annotations to indicate what kind of REST service a class method provides. The procedure “createPatient” in the diagram is annotated with @Create which indicates that it handles POST/create. This procedure then uses a PatientDAO (DAO - data access object) class which Spring Data uses to persist the Patient resource in MongoDB.

### Add jpa dependency

We will use elasticsearch as back end to store data. so add this dependency to **pom.xml**:

Note that if you need to save in a relational backend, the xml patient data need to be normalized and saved in tables you create. Otherwise you can use a jpa implementation avaiable (e.g. JPAResetServer in hapi-fhir-jpa-starter project)

**FHIR_POC/docker-compose.yml:**
```yml
version: '3.7'
services:
  elasticsearch:
    container_name: elasticsearch
    image: docker.elastic.co/elasticsearch/elasticsearch:6.8.5
    environment:
      discovery.type: single-node
    restart: "no"
    ports:
      - '9200:9200'
      - '9300:9300'
    expose:
      - '9200'
      - '9300'
  kibana:
    container_name: kibana
    image: docker.elastic.co/kibana/kibana:6.8.5
    ports:
      - "5601:5601"
    expose:
      - "5601"

networks:
  default:
```


**FhirServer/pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
    <version>2.4.6</version>
</dependency>
        <!--end: spring jpa dependencies -->
```

**FhirServer/src/main/resources/application.yml:**
```yml
spring:
  elasticsearch:
    rest:
      uris: http://localhost:9200

```

Patient