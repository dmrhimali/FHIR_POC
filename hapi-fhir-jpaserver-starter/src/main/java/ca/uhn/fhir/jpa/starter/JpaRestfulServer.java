package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

	 // https://groups.google.com/g/hapi-fhir/c/JKar0VmzpTE -- he uses  hapi-fhir-jpaserver-starter project version 5.3.0.
	 // https://blog.healthdataintegrator.com/2020/05/29/extending-the-hapi-fhir-jpa-server/
	  // https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations_operations.html
	  //Watch this and do something with it (a prototype): https://www.youtube.com/watch?v=bnLB98xQc9o (GQL and FHIR, 2021)
    // Add your own customization here

  }

}
