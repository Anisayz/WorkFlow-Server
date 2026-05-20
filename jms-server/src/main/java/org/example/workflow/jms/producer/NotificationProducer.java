package org.example.workflow.jms.producer;

import org.example.workflow.jms.broker.BrokerConfig;
import org.example.workflow.model.Notification;
import org.example.workflow.model.enums.NotificationType;
import org.example.workflow.util.JsonMapper;

import javax.jms.*;
import java.util.UUID;

public class NotificationProducer {

    /**
     * Envoie une notification JMS à un utilisateur spécifique.
     * Topic : "user.{recipientId}.notify"
     *
     * @param recipientId  UUID de l'utilisateur destinataire
     * @param type         TASK, MESSAGE ou SYSTEM
     * @param title        Titre de la notification
     * @param subtitle     Détail de la notification
     * @param referenceId  UUID de la tâche / message / équipe concernée
     */
    public void sendNotification(UUID recipientId, NotificationType type,
                                 String title, String subtitle, UUID referenceId) {
        String topicName = "user." + recipientId + ".notify";
        try {
            Connection connection = BrokerConfig.getConnectionFactory().createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            Notification notif = new Notification(
                    UUID.randomUUID(), recipientId, type, title, subtitle, referenceId
            );

            TextMessage msg = session.createTextMessage(JsonMapper.toJson(notif));
            producer.send(msg);

            System.out.println("[JMS] ✅ Envoyé → " + topicName + " | " + type + " | " + title);

            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("[JMS] ❌ Erreur envoi : " + e.getMessage());
        }
    }
}