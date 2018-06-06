package com.mycompany.k8sit;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

public class SpringConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SpringConfiguration.class);
    static final String AMQ_BROKERURL = "tcp://localhost:61616";

    @Bean
    ConnectionFactory connectionFactory(){
        String username = System.getenv("AMQ_USER");
        String password = System.getenv("AMQ_PASSWORD");

        log.info("username: {}, password: {}",username,password);


        ActiveMQConnectionFactory amqConnectionFactory = new ActiveMQConnectionFactory(username,password,AMQ_BROKERURL);
        return amqConnectionFactory;
    }

    @Bean
    JmsTemplate jmsTemplate(@Autowired ConnectionFactory connectionFactory){
        JmsTemplate jmsTemplate= new JmsTemplate(connectionFactory);
        jmsTemplate.setReceiveTimeout(60*1000);
        return jmsTemplate;
    }
}
