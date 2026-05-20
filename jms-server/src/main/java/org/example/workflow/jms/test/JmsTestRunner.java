package org.example.workflow.jms.test;

import org.apache.activemq.broker.BrokerService;
import org.example.workflow.jms.consumer.TopicSubscriber;
import org.example.workflow.jms.producer.NotificationProducer;
import org.example.workflow.model.enums.NotificationType;

import java.util.UUID;

public class JmsTestRunner {

    public static void main(String[] args) throws Exception {

        // 1. Démarrer le broker embedded
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.setPersistent(false);
        broker.start();
        System.out.println("🔌 Broker démarré");

        // 2. Fake users
        UUID user1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID user2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        // 3. Abonner les consumers
        TopicSubscriber sub1 = new TopicSubscriber();
        TopicSubscriber sub2 = new TopicSubscriber();
        sub1.subscribeToUser(user1);
        sub2.subscribeToUser(user2);
        Thread.sleep(500);

        // 4. Envoyer des fake notifications
        NotificationProducer producer = new NotificationProducer();

        producer.sendNotification(user1, NotificationType.TASK,
                "Nouvelle tâche", "Fix bug #42", UUID.randomUUID());
        Thread.sleep(300);

        producer.sendNotification(user2, NotificationType.MESSAGE,
                "Nouveau message", "Bob: PR prête !", UUID.randomUUID());
        Thread.sleep(300);

        producer.sendNotification(user1, NotificationType.SYSTEM,
                "Équipe mise à jour", "Alice a rejoint l'équipe", UUID.randomUUID());
        Thread.sleep(1000);

        System.out.println("✅ Test terminé !");
        sub1.close();
        sub2.close();
        broker.stop();
    }
}