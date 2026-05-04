package org.example.workflow.jms.consumer;

import org.example.workflow.model.Notification;
import org.example.workflow.util.JsonMapper;
import javax.jms.*;

public class NotificationConsumer implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                Notification notif = JsonMapper.fromJson(
                        textMessage.getText(), Notification.class
                );
                System.out.println("[JMS] 🔔 Notification reçue :");
                System.out.println("  → Type       : " + notif.getType());
                System.out.println("  → Titre      : " + notif.getTitle());
                System.out.println("  → Sous-titre : " + notif.getSubtitle());
                System.out.println("  → Pour user  : " + notif.getRecipientId());
                System.out.println("  → Référence  : " + notif.getReferenceId());
                System.out.println("  → Heure      : " + notif.getCreatedAt());
            }
        } catch (Exception e) {
            System.err.println("[JMS] ❌ Erreur traitement : " + e.getMessage());
        }
    }
}