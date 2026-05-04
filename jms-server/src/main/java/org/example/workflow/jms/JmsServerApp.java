package org.example.workflow.jms;

import org.apache.activemq.broker.BrokerService;
import org.example.workflow.jms.consumer.TopicSubscriber;
import org.example.workflow.state.AppState;
import org.example.workflow.state.StateLoader;

import java.util.UUID;

public class JmsServerApp {

    public static void main(String[] args) throws Exception {
        System.out.println("[JMS-SERVER] 🚀 Démarrage...");

        // Démarrer le broker ActiveMQ directement dans la JVM
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.setPersistent(false);
        broker.start();
        System.out.println("[JMS-SERVER] 🔌 Broker démarré !");

        // Charger les données
        StateLoader.load();

        // S'abonner aux topics des utilisateurs
        AppState state = AppState.getInstance();
        TopicSubscriber subscriber = new TopicSubscriber();
        for (UUID userId : state.getUsers().keySet()) {
            subscriber.subscribeToUser(userId);
        }

        System.out.println("[JMS-SERVER] ✅ Prêt !");
        Thread.currentThread().join();
    }
}