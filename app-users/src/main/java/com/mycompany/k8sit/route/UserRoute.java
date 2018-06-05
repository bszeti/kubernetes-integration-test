package com.mycompany.k8sit.route;

import com.mycompany.k8sit.model.Address;
import com.mycompany.k8sit.model.User;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class UserRoute extends RouteBuilder {
    private static final Logger log = LoggerFactory.getLogger(UserRoute.class);

    @Override
    public void configure() throws Exception {

        from("amq:user.in?consumerCount={{amq.consumerCount}}&transacted=true").routeId("user.in")
            .streamCaching()

            //Receive user email from body
            .log(LoggingLevel.DEBUG,log,"User received: ${body}")
            .unmarshal().json(JsonLibrary.Jackson,User.class)
            .to("bean-validator:user-in")

            //We populate this object during the route
            .setProperty("user",body())

            //Select phone number from database
            .to("sql:SELECT phone FROM users WHERE email=:#${exchangeProperty.user.email}?dataSource=#dataSource&outputType=SelectOne&outputHeader=phone")
            .script().simple("${exchangeProperty.user.setPhone(${header.phone})}")

            //Call api for address
            .removeHeaders("*")
            .setBody(constant(null))
            .setHeader(Exchange.HTTP_URI).constant("{{api.url}}")
            .setHeader(Exchange.HTTP_PATH).simple("address/email/${exchangeProperty.user.email}")
            .setHeader("CamelHttpMethod").constant("GET")
            .setHeader("Accept").simple("application/json")
            .to("http4:apiCall?synchronous=true")
            .log(LoggingLevel.DEBUG,log,"Address received: ${body}")
            .unmarshal().json(JsonLibrary.Jackson,Address.class)
            .script().simple("${exchangeProperty.user.setAddress(${body})}")


            //Send user with added fields to out queue
            .setBody(exchangeProperty("user"))
            .marshal().json(JsonLibrary.Jackson)
            .convertBodyTo(String.class)

            .removeHeaders("*")
            .log(LoggingLevel.DEBUG,log,"Send user: ${body}")
            .to("amq:user.out?prefillPool=false")
        ;
    }

}
