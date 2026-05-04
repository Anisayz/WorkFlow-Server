package org.example.workflow.jms.producer;

import org.example.workflow.jms.broker.BrokerConfig;
import javax.jms.*;

public class TopicPublisher {

    public void publish(String topicName, String jsonPayload) {
        try {
            Connection connection = BrokerConfig.getConnectionFactory().createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.send(session.createTextMessage(jsonPayload));

            System.out.println("[JMS] 📤 Publié → " + topicName);

            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("[JMS] ❌ Erreur publish : " + e.getMessage());
        }
    }
}