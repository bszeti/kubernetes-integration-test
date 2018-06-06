package com.mycompany.k8sit;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.TextMessage;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfiguration.class})
public class IntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);

    @Autowired
    JmsTemplate jmsTemplate;

    @Test
    public void testSucc() throws Exception {
        jmsTemplate.convertAndSend("user.in", "{\"email\":\"testSucc@test.com\"}");
        TextMessage message = (TextMessage) jmsTemplate.receive("user.out");
        String response = message.getText();

        log.info("Response: {}",response);

        assertEquals("testSucc@test.com", JsonPath.read(response, "$.email"));
        assertEquals("5551234567", JsonPath.read(response, "$.phone"));
        assertEquals("Test State", JsonPath.read(response, "$.address.state"));
        assertEquals("Test City", JsonPath.read(response, "$.address.city"));
        assertEquals("1 Test St", JsonPath.read(response, "$.address.address"));
        assertEquals("T001", JsonPath.read(response, "$.address.zip"));

    }

}
