package com.mycompany.k8sit;

import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Category(RequiresOpenshift.class)
@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class ArquillianTest {
    private static final Logger log = LoggerFactory.getLogger(ArquillianTest.class);


    @Test
    public void testSucc() throws Exception {
        log.info("Send testSucc");
    }

}
