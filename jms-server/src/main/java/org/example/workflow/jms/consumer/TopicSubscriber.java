package org.example.workflow.jms.consumer;

import org.example.workflow.jms.broker.BrokerConfig;
import javax.jms.*;
import java.util.UUID;

public class TopicSubscriber {

    private Connection connection;
    private Session session;

    /**
     * S'abonne au topic d'un utilisateur.
     * Topic : "user.{userId}.notify"
     */
    public void subscribeToUser(UUID userId) {
        String topicName = "user." + userId + ".notify";
        try {
            connection = BrokerConfig.getConnectionFactory().createConnection();
            connection.setClientID("server-consumer-" + userId);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);

            javax.jms.TopicSubscriber subscriber = session.createDurableSubscriber(
                    topic, "sub-" + userId
            );
            subscriber.setMessageListener(new NotificationConsumer());

            System.out.println("[JMS] 📡 Abonné → " + topicName);

        } catch (JMSException e) {
            System.err.println("[JMS] ❌ Erreur abonnement : " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException ignored) {}
    }
}