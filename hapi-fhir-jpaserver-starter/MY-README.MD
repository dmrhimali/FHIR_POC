## Update docker-compose.yml to use postgres

```yml
version: "3"
services:
  hapi-fhir-jpaserver-start:
    build: .
    container_name: hapi-fhir-jpaserver-start
    restart: on-failure
    ports:
      - "8080:8080"
    volumes:
      - ./src:/tmp/hapi-fhir-jpaserver-starter/src/
  hapi-fhir-postgresdb:
    image: postgres
    container_name: hapi-fhir-postgresdb
    environment:
      POSTGRES_PASSWORD: hapi
      POSTGRES_USER: hapi
      POSTGRES_DB: fhir
    ports:
      - "5432:5432"
    expose:
      - 5432
    volumes:
      - ./postgres-data:/var/lib/postgresql/data

volumes:
  hapi-fhir-postgresdb:

```

## Update pom.xml

changes
```xml
     <properties>
-        <java.version>8</java.version>
+        <java.version>11</java.version>
     </properties>

<dependencies>
+        <dependency>
    +            <groupId>org.springframework.boot</groupId>
    +            <artifactId>spring-boot-starter-web</artifactId>
    +            <version>${spring_boot_version}</version>
    +        </dependency>
</dependencies>

<profile>
<id>jetty</id>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>${spring_boot_version}</version>
        -                    <exclusions>
        -                        <exclusion>
        -                            <groupId>org.springframework.boot</groupId>
        -                            <artifactId>spring-boot-starter-tomcat</artifactId>
        -                        </exclusion>
        -                    </exclusions>

    </dependency>
</dependencies>
</profile>
```

**application.yml**

changes:
```yml
   datasource:
-    url: 'jdbc:h2:file:./target/database/h2'
+    url: 'jdbc:postgresql://host.docker.internal:5432/fhir'
     #url: jdbc:h2:mem:test_mem
-    username: sa
-    password: null
-    driverClassName: org.h2.Driver
+    username: hapi
+    password: hapi
+    driverClassName: org.postgresql.Driver

```

full file:
```yml
spring:
  datasource:
    url: 'jdbc:postgresql://host.docker.internal:5432/fhir'
    #url: jdbc:h2:mem:test_mem
    username: hapi
    password: hapi
    driverClassName: org.postgresql.Driver
    max-active: 15

    # database connection pool size
    hikari:
      maximum-pool-size: 10
  jpa:
    properties:
      hibernate.format_sql: false
      hibernate.show_sql: false
#      hibernate.dialect: org.hibernate.dialect.h2dialect
#      hibernate.hbm2ddl.auto: update
#      hibernate.jdbc.batch_size: 20
#      hibernate.cache.use_query_cache: false
#      hibernate.cache.use_second_level_cache: false
#      hibernate.cache.use_structured_entries: false
#      hibernate.cache.use_minimal_puts: false
###    These settings will enable fulltext search with lucene
#      hibernate.search.enabled: true
#      hibernate.search.backend.type: lucene
#      hibernate.search.backend.analysis.configurer: ca.uhn.fhir.jpa.search.HapiLuceneAnalysisConfigurer
#      hibernate.search.backend.directory.type: local-filesystem
#      hibernate.search.backend.directory.root: target/lucenefiles
#      hibernate.search.backend.lucene_version: lucene_current
  batch:
    job:
      enabled: false
  main:
#    TODO 5.6.0 -> Prevent duplicate bean definitions in the Spring batch config in HAPI: see:
    allow-bean-definition-overriding: true
hapi:
  fhir:
    ### This is the FHIR version. Choose between, DSTU2, DSTU3, R4 or R5
    fhir_version: R4
### enable to use the ApacheProxyAddressStrategy which uses X-Forwarded-* headers
### to determine the FHIR server address
#   use_apache_address_strategy: false
### forces the use of the https:// protocol for the returned server address.
### alternatively, it may be set using the X-Forwarded-Proto header.
#   use_apache_address_strategy_https: false
### enable to set the Server URL
#    server_address: http://hapi.fhir.org/baseR4
#    defer_indexing_for_codesystems_of_size: 101
#    install_transitive_ig_dependencies: true
#    implementationguides:
###    example from registry (packages.fhir.org)
#      swiss:
#        name: swiss.mednet.fhir
#        version: 0.8.0
#      example not from registry
#      ips_1_0_0:
#        url: https://build.fhir.org/ig/HL7/fhir-ips/package.tgz
#        name: hl7.fhir.uv.ips
#        version: 1.0.0
#    supported_resource_types:
#      - Patient
#      - Observation
#    allow_cascading_deletes: true
#    allow_contains_searches: true
#    allow_external_references: true
#    allow_multiple_delete: true
#    allow_override_default_search_params: true
#    auto_create_placeholder_reference_targets: false
#    cql_enabled: true
#    default_encoding: JSON
#    default_pretty_print: true
#    default_page_size: 20
#    delete_expunge_enabled: true
#    enable_repository_validating_interceptor: false
#    enable_index_missing_fields: false
#    enable_index_contained_resource: false
#    enforce_referential_integrity_on_delete: false
#    enforce_referential_integrity_on_write: false
#    etag_support_enabled: true
#    expunge_enabled: true
#    daoconfig_client_id_strategy: null
#    client_id_strategy: ALPHANUMERIC
#    fhirpath_interceptor_enabled: false
#    filter_search_enabled: true
#    graphql_enabled: true
#    narrative_enabled: true
#    mdm_enabled: true
#    partitioning:
#      allow_references_across_partitions: false
#      partitioning_include_in_search_hashes: false
    cors:
      allow_Credentials: true
      # These are allowed_origin patterns, see: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/cors/CorsConfiguration.html#setAllowedOriginPatterns-java.util.List-
      allowed_origin:
        - '*'

    # Search coordinator thread pool sizes
    search-coord-core-pool-size: 20
    search-coord-max-pool-size: 100
    search-coord-queue-capacity: 200

#    logger:
#      error_format: 'ERROR - ${requestVerb} ${requestUrl}'
#      format: >-
#        Path[${servletPath}] Source[${requestHeader.x-forwarded-for}]
#        Operation[${operationType} ${operationName} ${idOrResourceName}]
#        UA[${requestHeader.user-agent}] Params[${requestParameters}]
#        ResponseEncoding[${responseEncodingNoDefault}]
#      log_exceptions: true
#      name: fhirtest.access
#    max_binary_size: 104857600
#    max_page_size: 200
#    retain_cached_searches_mins: 60
#    reuse_cached_search_results_millis: 60000
    tester:
        home:
          name: Local Tester
          server_address: 'http://localhost:8080/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
        global:
          name: Global Tester
          server_address: "http://hapi.fhir.org/baseR4"
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
#    validation:
#      requests_enabled: true
#      responses_enabled: true
#    binary_storage_enabled: true
#    bulk_export_enabled: true
#    subscription:
#      resthook_enabled: true
#      websocket_enabled: false
#      email:
#        from: some@test.com
#        host: google.com
#        port:
#        username:
#        password:
#        auth:
#        startTlsEnable:
#        startTlsRequired:
#        quitWait:
#    lastn_enabled: true
###  This is configuration for normalized quantity serach level default is 0
###   0: NORMALIZED_QUANTITY_SEARCH_NOT_SUPPORTED - default
###   1: NORMALIZED_QUANTITY_STORAGE_SUPPORTED
###   2: NORMALIZED_QUANTITY_SEARCH_SUPPORTED
#    normalized_quantity_search_level: 2
#elasticsearch:
#  debug:
#    pretty_print_json_log: false
#    refresh_after_write: false
#  enabled: false
#  password: SomePassword
#  required_index_status: YELLOW
#  rest_url: 'localhost:9200'
#  protocol: 'http'
#  schema_management_strategy: CREATE
#  username: SomeUsername
```
**docker-compose up** 

check postgresdocker image

**docker-compose up hapi-fhir-postgresdb**

cconnect using dbvisualizer and checck default tables

dbserver: localhost port : 5432 database: fhir user: hapi pass: hapi

build jpa-hapi-fhir-starter docker image

if you have one already build, remove image:

**docker rmi hapi-fhir-jpaserver-starter_hapi-fhir-jpaserver-start**

**docker stop hapi-fhir-postgresdb**

**docker rm hapi-fhir-postgresdb**

```shell
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  git diff
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  docker images
REPOSITORY                                              TAG       IMAGE ID       CREATED         SIZE
hapi-fhir-jpaserver-starter_hapi-fhir-jpaserver-start   latest    1d8f2fba2291   5 hours ago     651MB
debezium/example-postgres                               latest    19ad1e5a7e4a   2 days ago      478MB
postgres                                                latest    6ce504119cc8   6 days ago      374MB
debezium/connect                                        latest    099e8f585f46   2 weeks ago     785MB
debezium/kafka                                          latest    fad3f492a80e   2 weeks ago     679MB
debezium/zookeeper                                      latest    d53387f5d985   2 weeks ago     490MB
debezium/connect                                        1.0       3aa5a788d976   15 months ago   683MB
debezium/example-postgres                               1.0       4243a4653984   15 months ago   477MB
debezium/zookeeper                                      1.0       c13b365d20c2   15 months ago   488MB
debezium/kafka                                          1.0       0800b61eebbe   15 months ago   660MB
docker.elastic.co/kibana/kibana                         6.8.5     5983f5fff662   23 months ago   726MB
docker.elastic.co/elasticsearch/elasticsearch           6.8.5     4f90d9a6692f   23 months ago   866MB
postgres                                                10.5      3fce051f5a48   2 years ago     228MB
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  docker rmi hapi-fhir-jpaserver-starter_hapi-fhir-jpaserver-start
Error response from daemon: conflict: unable to remove repository reference "hapi-fhir-jpaserver-starter_hapi-fhir-jpaserver-start" (must force) - container b5763243f8f2 is using its referenced image 1d8f2fba2291
 ✘ rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  docker stop b5763243f8f2
b5763243f8f2
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  docker rm b5763243f8f2
b5763243f8f2
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  docker rmi hapi-fhir-jpaserver-starter_hapi-fhir-jpaserver-start
Untagged: hapi-fhir-jpaserver-starter_hapi-fhir-jpaserver-start:latest
Deleted: sha256:1d8f2fba22911d42d3df9e570beb8374e3697559d7c88779e236f4444c4c8774
 rasanjalee.dissanaya@VP2461  ~/home/code/FHIR_POC/hapi-fhir-jpaserver-starter   master ±✚  

```
**docker-compose build --no-cache**

stop postgres docker, and bring up both containers:

docker-compose stop or CTRL+C

**docker-compose-up**

for jpa-fhir container logs should show it is up

hapi-fhir-jpaserver-start | 2021-08-12 20:48:06.009 [main] INFO  o.s.web.servlet.DispatcherServlet [FrameworkServlet.java:547] Completed initialization in 236 ms
hapi-fhir-jpaserver-start | 12-Aug-2021 20:48:06.023 INFO [main] org.apache.catalina.startup.HostConfig.deployWAR Deployment of web application archive [/usr/local/tomcat/webapps/ROOT.war] has finished in [27,104] ms
hapi-fhir-jpaserver-start | 12-Aug-2021 20:48:06.027 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["http-nio-8080"]
hapi-fhir-jpaserver-start | 12-Aug-2021 20:48:06.037 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in [27215] milliseconds

see: https://bitbucket.org/dmrhimali/fhir/src/master/hapi-fhir-jpaserver-starter-master/ for steps

## How to implement a custom resource
https://stackoverflow.com/questions/54153559/hapi-fhir-domainresource-what-url-do-you-use

Create custom resource:

**MyPatient.java**
```java
package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition class for adding extensions to the built-in
 * Patient resource type.
 *
 * Note the "profile" attribute below, which indicates the URL/ID of the
 * profile implemented by this resource. You are not required to supply this,
 * but if you do it will be automatically populated in the resource meta
 * tag if the resource is returned by a server.
 */
@ResourceDef(name="Patient", profile="http://example.com/StructureDefinition/mypatient")
public class MyPatient extends Patient {

	private static final long serialVersionUID = 1L;

	/**
	 * Each extension is defined in a field. Any valid HAPI Data Type
	 * can be used for the field type. Note that the [name=""] attribute
	 * in the @Child annotation needs to match the name for the bean accessor
	 * and mutator methods.
	 */
	@Child(name="petName")
	@Extension(url="http://example.com/dontuse#petname", definedLocally=false, isModifier=false)
	@Description(shortDefinition="The name of the patient's favourite pet")
	private StringType myPetName;

	/**
	 * The second example extension uses a List type to provide
	 * repeatable values. Note that a [max=] value has been placed in
	 * the @Child annotation.
	 *
	 * Note also that this extension is a modifier extension
	 */
	@Child(name="importantDates", max=Child.MAX_UNLIMITED)
	@Extension(url="http://example.com/dontuse#importantDates", definedLocally=false, isModifier=true)
	@Description(shortDefinition="Some dates of note for this patient")
	private List<DateTimeType> myImportantDates;

	/**
	 * It is important to override the isEmpty() method, adding a check for any
	 * newly added fields.
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && ElementUtil.isEmpty(myPetName, myImportantDates);
	}

	/********
	 * Accessors and mutators follow
	 *
	 * IMPORTANT:
	 * Each extension is required to have an getter/accessor and a setter/mutator.
	 * You are highly recommended to create getters which create instances if they
	 * do not already exist, since this is how the rest of the HAPI FHIR API works.
	 ********/

	/** Getter for important dates */
	public List<DateTimeType> getImportantDates() {
		if (myImportantDates==null) {
			myImportantDates = new ArrayList<DateTimeType>();
		}
		return myImportantDates;
	}

	/** Getter for pet name */
	public StringType getPetName() {
		if (myPetName == null) {
			myPetName = new StringType();
		}
		return myPetName;
	}

	/** Setter for important dates */
	public void setImportantDates(List<DateTimeType> theImportantDates) {
		myImportantDates = theImportantDates;
	}

	/** Setter for pet name */
	public void setPetName(StringType thePetName) {
		myPetName = thePetName;
	}

}
```

Create a provider for custom resource:

**MyPatientResourceProvider.java**
```java
package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu2.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StringType;

public class MyPatientResourceProvider implements IResourceProvider{
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MyPatient.class;
	}

	@Read()
	public MyPatient getResourceById(@IdParam IdType theId) {
		MyPatient patient = new MyPatient();
		patient.setId(theId);
		patient.setPetName(new StringType("Nelly"));
		patient.addIdentifier();
		patient.getIdentifier().get(0).setSystem(String.valueOf(new UriDt("urn:hapitest:mrns")));
		patient.getIdentifier().get(0).setValue("00002");
		patient.addName().setFamily("Test");
		patient.getName().get(0).addGiven("PatientOne");
		return patient;
	}

}

```

Register new provider:

**/src/main/java/ca/uhn/fhir/jpa/starter/BaseJpaRestfulServer.java**
```java
package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.cql.common.provider.CqlProviderLoader;
import ca.uhn.fhir.interceptor.api.IInterceptorBroadcaster;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.binstore.BinaryStorageInterceptor;
import ca.uhn.fhir.jpa.bulk.export.provider.BulkDataExportProvider;
import ca.uhn.fhir.jpa.interceptor.CascadingDeleteInterceptor;
import ca.uhn.fhir.jpa.packages.IPackageInstallerSvc;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.provider.GraphQLProvider;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.jpa.provider.JpaConformanceProviderDstu2;
import ca.uhn.fhir.jpa.provider.SubscriptionTriggeringProvider;
import ca.uhn.fhir.jpa.provider.TerminologyUploaderProvider;
import ca.uhn.fhir.jpa.provider.dstu3.JpaConformanceProviderDstu3;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.subscription.util.SubscriptionDebugLogInterceptor;
import ca.uhn.fhir.mdm.provider.MdmProviderLoader;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.narrative2.NullNarrativeGenerator;
import ca.uhn.fhir.rest.server.ApacheProxyAddressStrategy;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.FhirPathFilterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.partition.RequestTenantPartitionInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;

public class BaseJpaRestfulServer extends RestfulServer {
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BaseJpaRestfulServer.class);

	private static final long serialVersionUID = 1L;
  @Autowired
  DaoRegistry daoRegistry;
  @Autowired
  DaoConfig daoConfig;
  @Autowired
  ISearchParamRegistry searchParamRegistry;
  @Autowired
  IFhirSystemDao fhirSystemDao;
  @Autowired
  ResourceProviderFactory resourceProviderFactory;
  @Autowired
  IJpaSystemProvider jpaSystemProvider;
  @Autowired
  IInterceptorBroadcaster interceptorBroadcaster;
  @Autowired
  DatabaseBackedPagingProvider databaseBackedPagingProvider;
  @Autowired
  IInterceptorService interceptorService;
  @Autowired
  IValidatorModule validatorModule;
  @Autowired
  Optional<GraphQLProvider> graphQLProvider;
  @Autowired
  BulkDataExportProvider bulkDataExportProvider;
  @Autowired
  PartitionManagementProvider partitionManagementProvider;
  @Autowired
  BinaryStorageInterceptor binaryStorageInterceptor;
  @Autowired
  IPackageInstallerSvc packageInstallerSvc;
  @Autowired
  AppProperties appProperties;
  @Autowired
  ApplicationContext myApplicationContext;
  @Autowired(required = false)
  IRepositoryValidationInterceptorFactory factory;
  // These are set only if the features are enabled
  @Autowired
  Optional<CqlProviderLoader> cqlProviderLoader;
  @Autowired
  Optional<MdmProviderLoader> mdmProviderProvider;

  @Autowired
  private IValidationSupport myValidationSupport;

  public BaseJpaRestfulServer() {
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    /*
     * Create a FhirContext object that uses the version of FHIR
     * specified in the properties file.
     */
    // Customize supported resource types
    List<String> supportedResourceTypes = appProperties.getSupported_resource_types();

    if (!supportedResourceTypes.isEmpty() && !supportedResourceTypes.contains("SearchParameter")) {
      supportedResourceTypes.add("SearchParameter");
      daoRegistry.setSupportedResourceTypes(supportedResourceTypes);
    }

    setFhirContext(fhirSystemDao.getContext());

    /*
     * Order matters - the MDM provider registers itself on the resourceProviderFactory - hence the loading must be done
     * ahead of provider registration
     */
    if(appProperties.getMdm_enabled())
    	mdmProviderProvider.get().loadProvider();

    registerProviders(resourceProviderFactory.createProviders());
    registerProvider(jpaSystemProvider);
    
    //Register Custom resource(MyPatient) provided for 
    registerProvider(new MyPatientResourceProvider());

    /*
     * The conformance provider exports the supported resources, search parameters, etc for
     * this server. The JPA version adds resourceProviders counts to the exported statement, so it
     * is a nice addition.
     *
     * You can also create your own subclass of the conformance provider if you need to
     * provide further customization of your server's CapabilityStatement
     */

    FhirVersionEnum fhirVersion = fhirSystemDao.getContext().getVersion().getVersion();
    if (fhirVersion == FhirVersionEnum.DSTU2) {

      JpaConformanceProviderDstu2 confProvider = new JpaConformanceProviderDstu2(this, fhirSystemDao,
        daoConfig);
      confProvider.setImplementationDescription("HAPI FHIR DSTU2 Server");
      setServerConformanceProvider(confProvider);
    } else {
      if (fhirVersion == FhirVersionEnum.DSTU3) {

        JpaConformanceProviderDstu3 confProvider = new JpaConformanceProviderDstu3(this, fhirSystemDao,
          daoConfig, searchParamRegistry);
        confProvider.setImplementationDescription("HAPI FHIR DSTU3 Server");
        setServerConformanceProvider(confProvider);
      } else if (fhirVersion == FhirVersionEnum.R4) {

				JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(this, fhirSystemDao,
					daoConfig, searchParamRegistry, myValidationSupport);
        confProvider.setImplementationDescription("HAPI FHIR R4 Server");
        setServerConformanceProvider(confProvider);
      } else if (fhirVersion == FhirVersionEnum.R5) {

				JpaCapabilityStatementProvider confProvider = new JpaCapabilityStatementProvider(this, fhirSystemDao,
					daoConfig, searchParamRegistry, myValidationSupport);
        confProvider.setImplementationDescription("HAPI FHIR R5 Server");
        setServerConformanceProvider(confProvider);
      } else {
        throw new IllegalStateException();
      }
    }

    /*
     * ETag Support
     */

    if (appProperties.getEtag_support_enabled() == false)
      setETagSupport(ETagSupportEnum.DISABLED);


    /*
     * This server tries to dynamically generate narratives
     */
    FhirContext ctx = getFhirContext();
    INarrativeGenerator theNarrativeGenerator =
      appProperties.getNarrative_enabled() ?
      new DefaultThymeleafNarrativeGenerator() :
      new NullNarrativeGenerator();
    ctx.setNarrativeGenerator(theNarrativeGenerator);

    /*
     * Default to JSON and pretty printing
     */
    setDefaultPrettyPrint(appProperties.getDefault_pretty_print());

    /*
     * Default encoding
     */
    setDefaultResponseEncoding(appProperties.getDefault_encoding());

    /*
     * This configures the server to page search results to and from
     * the database, instead of only paging them to memory. This may mean
     * a performance hit when performing searches that return lots of results,
     * but makes the server much more scalable.
     */

    setPagingProvider(databaseBackedPagingProvider);

    /*
     * This interceptor formats the output using nice colourful
     * HTML output when the request is detected to come from a
     * browser.
     */
    ResponseHighlighterInterceptor responseHighlighterInterceptor = new ResponseHighlighterInterceptor();
    this.registerInterceptor(responseHighlighterInterceptor);

    if (appProperties.getFhirpath_interceptor_enabled()) {
      registerInterceptor(new FhirPathFilterInterceptor());
    }

    /*
     * Add some logging for each request
     */
    LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
    loggingInterceptor.setLoggerName(appProperties.getLogger().getName());
    loggingInterceptor.setMessageFormat(appProperties.getLogger().getFormat());
    loggingInterceptor.setErrorMessageFormat(appProperties.getLogger().getError_format());
    loggingInterceptor.setLogExceptions(appProperties.getLogger().getLog_exceptions());
    this.registerInterceptor(loggingInterceptor);

    /*
     * If you are hosting this server at a specific DNS name, the server will try to
     * figure out the FHIR base URL based on what the web container tells it, but
     * this doesn't always work. If you are setting links in your search bundles that
     * just refer to "localhost", you might want to use a server address strategy:
     */
    String serverAddress = appProperties.getServer_address();
    if (!Strings.isNullOrEmpty(serverAddress)) {
      setServerAddressStrategy(new HardcodedServerAddressStrategy(serverAddress));
    } else if (appProperties.getUse_apache_address_strategy()) {
      boolean useHttps = appProperties.getUse_apache_address_strategy_https();
      setServerAddressStrategy(useHttps ? ApacheProxyAddressStrategy.forHttps() :
                    ApacheProxyAddressStrategy.forHttp());
    } else {
      setServerAddressStrategy(new IncomingRequestAddressStrategy());
    }

    /*
     * If you are using DSTU3+, you may want to add a terminology uploader, which allows
     * uploading of external terminologies such as Snomed CT. Note that this uploader
     * does not have any security attached (any anonymous user may use it by default)
     * so it is a potential security vulnerability. Consider using an AuthorizationInterceptor
     * with this feature.
     */
    if (ctx.getVersion().getVersion().isEqualOrNewerThan(FhirVersionEnum.DSTU3)) { // <-- ENABLED RIGHT NOW
      registerProvider(myApplicationContext.getBean(TerminologyUploaderProvider.class));
    }

    // If you want to enable the $trigger-subscription operation to allow
    // manual triggering of a subscription delivery, enable this provider
    if (true) { // <-- ENABLED RIGHT NOW
      registerProvider(myApplicationContext.getBean(SubscriptionTriggeringProvider.class));
    }

    // Define your CORS configuration. This is an example
    // showing a typical setup. You should customize this
    // to your specific needs
    if (appProperties.getCors() != null) {
    	ourLog.info("CORS is enabled on this server");
      CorsConfiguration config = new CorsConfiguration();
      config.addAllowedHeader(HttpHeaders.ORIGIN);
      config.addAllowedHeader(HttpHeaders.ACCEPT);
      config.addAllowedHeader(HttpHeaders.CONTENT_TYPE);
      config.addAllowedHeader(HttpHeaders.AUTHORIZATION);
      config.addAllowedHeader(HttpHeaders.CACHE_CONTROL);
      config.addAllowedHeader("x-fhir-starter");
      config.addAllowedHeader("X-Requested-With");
      config.addAllowedHeader("Prefer");

      List<String> allAllowedCORSOrigins = appProperties.getCors().getAllowed_origin();
      allAllowedCORSOrigins.forEach(config::addAllowedOriginPattern);
      ourLog.info("CORS allows the following origins: " + String.join(", ", allAllowedCORSOrigins));

      config.addExposedHeader("Location");
      config.addExposedHeader("Content-Location");
      config.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
      config.setAllowCredentials(appProperties.getCors().getAllow_Credentials());

      // Create the interceptor and register it
      CorsInterceptor interceptor = new CorsInterceptor(config);
      registerInterceptor(interceptor);
    } else {
    	ourLog.info("CORS is disabled on this server");
	 }

    // If subscriptions are enabled, we want to register the interceptor that
    // will activate them and match results against them
    if (appProperties.getSubscription() != null) {
      // Subscription debug logging
      interceptorService.registerInterceptor(new SubscriptionDebugLogInterceptor());
    }

    // Cascading deletes


    if (appProperties.getAllow_cascading_deletes()) {
      CascadingDeleteInterceptor cascadingDeleteInterceptor = new CascadingDeleteInterceptor(ctx,
        daoRegistry, interceptorBroadcaster);
      getInterceptorService().registerInterceptor(cascadingDeleteInterceptor);
    }

    // Binary Storage
    if (appProperties.getBinary_storage_enabled()) {
      getInterceptorService().registerInterceptor(binaryStorageInterceptor);
    }

    // Validation

    if (validatorModule != null) {
      if (appProperties.getValidation().getRequests_enabled()) {
        RequestValidatingInterceptor interceptor = new RequestValidatingInterceptor();
        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        interceptor.setValidatorModules(Collections.singletonList(validatorModule));
        registerInterceptor(interceptor);
      }
      if (appProperties.getValidation().getResponses_enabled()) {
        ResponseValidatingInterceptor interceptor = new ResponseValidatingInterceptor();
        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
        interceptor.setValidatorModules(Collections.singletonList(validatorModule));
        registerInterceptor(interceptor);
      }
    }

    // GraphQL
    if (appProperties.getGraphql_enabled()) {
      if (fhirVersion.isEqualOrNewerThan(FhirVersionEnum.DSTU3)) {
        registerProvider(graphQLProvider.get());
      }
    }

    if (appProperties.getAllowed_bundle_types() != null) {
      daoConfig.setBundleTypesAllowedForStorage(appProperties.getAllowed_bundle_types().stream().map(BundleType::toCode).collect(Collectors.toSet()));
    }

    daoConfig.setDeferIndexingForCodesystemsOfSize(appProperties.getDefer_indexing_for_codesystems_of_size());

    // Bulk Export
    if (appProperties.getBulk_export_enabled()) {
      registerProvider(bulkDataExportProvider);
    }

    // Partitioning
    if (appProperties.getPartitioning() != null) {
      registerInterceptor(new RequestTenantPartitionInterceptor());
      setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
      registerProviders(partitionManagementProvider);
    }

    if (appProperties.getClient_id_strategy() == DaoConfig.ClientIdStrategyEnum.ANY) {
      daoConfig.setResourceServerIdStrategy(DaoConfig.IdStrategyEnum.UUID);
      daoConfig.setResourceClientIdStrategy(appProperties.getClient_id_strategy());
    }

    if (appProperties.getImplementationGuides() != null) {
      Map<String, AppProperties.ImplementationGuide> guides = appProperties.getImplementationGuides();
      for (Map.Entry<String, AppProperties.ImplementationGuide> guide : guides.entrySet()) {
			PackageInstallationSpec packageInstallationSpec = new PackageInstallationSpec()
				.setPackageUrl(guide.getValue().getUrl())
				.setName(guide.getValue().getName())
				.setVersion(guide.getValue().getVersion())
				.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_AND_INSTALL);
			if(appProperties.getInstall_transitive_ig_dependencies()) {
				packageInstallationSpec.setFetchDependencies(true);
				packageInstallationSpec.setDependencyExcludes(ImmutableList.of("hl7.fhir.r2.core", "hl7.fhir.r3.core", "hl7.fhir.r4.core", "hl7.fhir.r5.core"));
			}
			packageInstallerSvc.install(packageInstallationSpec);
      }
    }

    if(factory != null) {
		 interceptorService.registerInterceptor(factory.buildUsingStoredStructureDefinitions());
	 }


    if (appProperties.getLastn_enabled()) {
      daoConfig.setLastNEnabled(true);
    }

    daoConfig.getModelConfig().setNormalizedQuantitySearchLevel(appProperties.getNormalized_quantity_search_level());

		daoConfig.getModelConfig().setIndexOnContainedResources(appProperties.getEnable_index_contained_resource());
  }
}

```

## Call custom resource

