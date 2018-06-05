package com.mycompany.k8sit;

import com.jayway.jsonpath.JsonPath;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertEquals;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("unittest")
@UseAdviceWith
@MockEndpointsAndSkip("amq:*")
@DirtiesContext
public class ApplicationTest {
    private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

    @Autowired
    private CamelContext context;

    @Produce(uri = "direct:user.in")
    private ProducerTemplate producer;

    @EndpointInject(uri = "mock:amq:user.out")
    private MockEndpoint mockUserOut;

    @EndpointInject(uri = "mock:addressByEmail")
    private MockEndpoint addressByEmail;



    @TestConfiguration
    static class TestSpringConfiguration {

        @Bean
        public RouteBuilder addTestHttpService() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    //See ServletMappingConfiguration mapping properties: camel.component.servlet.mapping
                    restConfiguration().component("servlet") //Requires "CamelServlet" to be registered
                        .bindingMode(RestBindingMode.off);

                    rest("address")
                        .produces(MediaType.APPLICATION_JSON_VALUE)
                        .get("email/{email}")
                            .route().routeId("test-http")
                            .setBody().constant("{\"address\":\"1428 Elm Street\"}")
                            .to("mock:addressByEmail")
                            .endRest();
                }
            };
        }
    }


    @Before
    public void before() throws Exception{

        if (context.getStatus()==ServiceStatus.Stopped) {
            //Execute adviseWith only once
            context.getRouteDefinition("user.in").adviceWith(context, new AdviceWithRouteBuilder() {
                @Override
                public void configure() throws Exception {
                    replaceFromWith("direct:user.in");
                }
            });


        }

        //Reset mock endpoint
        mockUserOut.reset();
        addressByEmail.reset();
    }

    @Test
    public void testSucc() throws Exception{
        context.start();
        mockUserOut.expectedMessageCount(1);

        producer.sendBody("{\"email\":\"testSucc@test.com\"}");

        mockUserOut.assertIsSatisfied();
        Exchange sentToSync = mockUserOut.getExchanges().get(0);
        String response = sentToSync.getIn().getBody(String.class);

        assertEquals("testSucc@test.com",JsonPath.read(response,"$.email"));
        assertEquals("5551234567",JsonPath.read(response,"$.phone"));
        assertEquals("1428 Elm Street",JsonPath.read(response,"$.address.address"));

    }


}
