package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu2.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StringType;

public class MyPatientResourceProvider implements IResourceProvider{

	String FHIR_SERVER_BASE_URL = "http://localhost:8080/fhir";

	FhirContext ctx;
	IGenericClient client;

	public  MyPatientResourceProvider(){
		ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		client = ctx.newRestfulGenericClient(FHIR_SERVER_BASE_URL);
	}

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

	@Create
	public MethodOutcome createPatient(@ResourceParam MyPatient patient) {
		return  client.create()
			.resource(patient)
			.prettyPrint()
			.encodedJson()
			.execute();
	}
}
