package ru.red.orderservice.producer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class OrderProducer {

    private final Producer<String, String> producer;

    @Autowired
    public OrderProducer(ProducerFactory<String, String> producerFactory) {
        this.producer = producerFactory.createProducer();
    }

    public void produce() {
        producer.send(new ProducerRecord<String, String>("order-manipulation", "test", "test"));
    }
}
