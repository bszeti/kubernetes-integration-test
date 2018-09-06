package com.mycompany.k8sit;

import com.jayway.jsonpath.JsonPath;
import io.fabric8.kubernetes.api.model.v4_0.PodList;
import io.fabric8.kubernetes.api.model.v4_0.ServiceList;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import okhttp3.Response;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.IOUtils;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.codehaus.plexus.util.StringInputStream;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.TextMessage;
import java.io.*;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Category(RequiresOpenshift.class)
@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
//@RunWith(Arquillian.class)
public class ArquillianTest {
    private static final Logger log = LoggerFactory.getLogger(ArquillianTest.class);

    @ArquillianResource
    io.fabric8.openshift.client.OpenShiftClient oc;
    @ArquillianResource
    io.fabric8.kubernetes.client.KubernetesClient client;

    //Causes: java.lang.IllegalArgumentException: Can not set io.fabric8.openshift.clnt.v4_0.OpenShiftClient field com.mycompany.k8sit.ArquillianTest.oc4 to io.fabric8.kubernetes.clnt.v4_0.DefaultKubernetesClient
//    @ArquillianResource
//    io.fabric8.kubernetes.clnt.v4_0.KubernetesClient client4;
//    @ArquillianResource
//    io.fabric8.openshift.clnt.v4_0.OpenShiftClient oc4;


    //Arquillian namespace: session.getNamespace() or oc.getNamespace()
    @ArquillianResource
    Session session;

    //Same as oc.services().list()
    @ArquillianResource
    ServiceList services;

    //Same as oc.pods().list()
    @ArquillianResource
    PodList pods;

    // @AwaitRoute //It requires a GET endpoint, but mock-server has none by default
    @RouteURL("mockserverroute")
    URL mockserver;

    @BeforeClass
    public static void beforeClass(){
        //clients are not available yet (even if static)
    }

    private boolean beforeDone = false;
    @Before
    public void before() throws Exception{
        if (!beforeDone){

            log.info("Before is running: {}",client);
            log.info("Before is running: {}",oc);

            // Prepare database;
            // Run mysql client in container with oc cli
            // oc exec waits for the command to finish
//            Runtime rt = Runtime.getRuntime();
//            Process mysql = rt.exec("oc exec -i -n "+session.getNamespace()+" mariadb -- /opt/rh/rh-mariadb102/root/usr/bin/mysql -u myuser -pmypassword -h 127.0.0.1 testdb");
//            IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("sql/sql-load.sql"),mysql.getOutputStream() );
//            mysql.getOutputStream().close();
//            log.info("waitFor: {}",mysql.waitFor());
//            log.info("output: {}", IOUtils.toString(mysql.getInputStream()));
//            log.info("error: {}", IOUtils.toString(mysql.getErrorStream()));

            // Prepare database
            // Run command in container using OpenShiftClient - run sql in mysql
            log.info("Sql load - start");
            final CountDownLatch latch = new CountDownLatch(1);
            OutputStream execOut = new ByteArrayOutputStream();
            OutputStream execErr = new ByteArrayOutputStream();
            ExecWatch exec = oc.pods().withName("mariadb")
                    .readingInput(this.getClass().getClassLoader().getResourceAsStream("sql/sql-load.sql"))
                    .writingOutput(execOut)
                    .writingError(execErr)
                    //.withTTY() //Optional
                    .usingListener(createCountDownListener(latch))
                    .exec("/opt/rh/rh-mariadb102/root/usr/bin/mysql","-u", "myuser", "-pmypassword", "-h", "127.0.0.1", "testdb")
                    ;
            if (!latch.await(20, TimeUnit.SECONDS)) {
                throw new Exception("Exec timeout");
            }
            log.info("Exec out: {}", ((ByteArrayOutputStream) execOut).toString());
            log.info("Exec err: {}", ((ByteArrayOutputStream) execErr).toString());
            log.info("Sql load - end");


            //Prepare MockServer response for test
            log.info("mockserver URL: {}",mockserver);
            int port = mockserver.getPort() == -1 ? 80 : mockserver.getPort();
            log.info("mockserver {} {}",mockserver.getHost(),port);
            MockServerClient mockServerClient = new MockServerClient(mockserver.getHost(),port);

            mockServerClient
                    .when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/v1/address/email/testSucc@test.com")
                    )
                    .respond(
                            response()
                                    .withBody("{\n" +
                                            "\"state\": \"Test State\",\n" +
                                            "\"city\": \"Test City\",\n" +
                                            "\"address\": \"1 Test St\",\n" +
                                            "\"zip\": \"T001\"\n" +
                                            "}\n")
                    );

            beforeDone=true;
        }
    }
    @Test
    public void testSucc() throws Exception {

        log.info("Test resources are in namespace:"+session.getNamespace());

        //OpenShift client - try
        log.info("OpenShift - getMasterUrl: {}",  oc.getMasterUrl());
        log.info("OpenShift - getNamespace: {}",  oc.getNamespace());
        log.info("OpenShift - getApiVersion: {}",  oc.getApiVersion());
        log.info("OpenShift - getConfiguration: {}",  oc.getConfiguration());
        log.info("OpenShift - getClass: {}",  oc.getClass());

        //Kubernetes client - try
        log.info("Kubernetes - getMasterUrl: {}",  client.getMasterUrl());
        log.info("Kubernetes - getNamespace: {}",  client.getNamespace());
        log.info("Kubernetes - getApiVersion: {}",  client.getApiVersion());
        log.info("Kubernetes - getConfiguration: {}",  client.getConfiguration());
        log.info("Kubernetes - getClass: {}",  client.getClass());

        //Service in the current namespace
        oc.services().list().getItems().stream()
                .map(s->s.getMetadata().getNamespace()+" - "+s.getMetadata().getName())
                .forEach(s->log.info("Service: {}",s));

        /*************
         * Start test
         *************/
        //Build amq brokerUrl from master url and service nodeport
        int amqPort = oc.services().withName("amqsvc").get().getSpec().getPorts().get(0).getNodePort();
//        String brokerUrl = "tcp://"+oc.getMasterUrl().getHost()+":"+amqPort;
        String brokerUrl = "tcp://10.0.2.15:"+amqPort;
        log.info("brokerUrl: {}",brokerUrl);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("test","secret",brokerUrl);
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);

        //Send test message and receive outcome
        jmsTemplate.convertAndSend("user.in", "{\"email\":\"testSucc@test.com\"}");
        TextMessage message = (TextMessage) jmsTemplate.receive("user.out");
        String response = message.getText();

        log.info("Response: {}",response);

        //Asserts
        assertEquals("testSucc@test.com", JsonPath.read(response, "$.email"));
        assertEquals("5551234567", JsonPath.read(response, "$.phone"));
        assertEquals("Test State", JsonPath.read(response, "$.address.state"));
        assertEquals("Test City", JsonPath.read(response, "$.address.city"));
        assertEquals("1 Test St", JsonPath.read(response, "$.address.address"));
        assertEquals("T001", JsonPath.read(response, "$.address.zip"));

    }

    public static ExecListener createCountDownListener(CountDownLatch latch){
        return new ExecListener() {
            @Override
            public void onOpen(Response response) {
                log.info("onOpen response: {}",response);
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                log.info("onFailure response: {}",response,t);
                latch.countDown();

            }

            @Override
            public void onClose(int code, String reason) {
                log.info("onClose reason: {} {}",code, reason);
                latch.countDown();

            }
        };
    }

}
