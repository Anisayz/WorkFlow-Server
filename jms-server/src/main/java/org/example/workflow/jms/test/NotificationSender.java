package org.example.workflow.jms.test;

import org.example.workflow.jms.producer.NotificationProducer;
import org.example.workflow.model.enums.NotificationType;

import java.util.UUID;

public class NotificationSender {

    public static void main(String[] args) throws Exception {

        // Même UUID que dans HelloApplication
        UUID user1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

        NotificationProducer producer = new NotificationProducer();

        producer.sendNotification(user1, NotificationType.TASK,
                "New task assigned", "Fix login bug — due May 5",
                UUID.randomUUID());
        Thread.sleep(300);

        producer.sendNotification(user1, NotificationType.MESSAGE,
                "Alice sent a message", "Hey, can you review PR #12?",
                UUID.randomUUID());
        Thread.sleep(300);

        producer.sendNotification(user1, NotificationType.SYSTEM,
                "Team updated", "Bob joined Alpha Squad",
                UUID.randomUUID());

        System.out.println("[Sender] ✅ 3 notifications envoyées !");
    }
}