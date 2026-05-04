package org.example.workflow.jms.broker;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.ConnectionFactory;

public class BrokerConfig {

    public static final String BROKER_URL = "tcp://localhost:61616";

    public static ConnectionFactory getConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        factory.setTrustAllPackages(true);
        return factory;
    }
}