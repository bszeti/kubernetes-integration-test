package com.mycompany.k8sit;

import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.openshift.impl.client.OpenShiftClient;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(RequiresOpenshift.class)
@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
//@RunWith(Arquillian.class)
public class ArquillianTest {
    private static final Logger log = LoggerFactory.getLogger(ArquillianTest.class);

    @ArquillianResource
    OpenShiftClient client;

    @ArquillianResource
    Session session;

    @Test
    public void testSucc() throws Exception {
        log.info("Starting");
        log.info("Test resources are in namespace:"+session.getNamespace());
        log.info("Pods:"+client.getClient().pods().list());
        log.info("Services:"+client.getClient().services().list());
    }

}
