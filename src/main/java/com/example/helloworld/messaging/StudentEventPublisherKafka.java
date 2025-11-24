package com.example.helloworld.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class StudentEventPublisherKafka implements StudentEventPublisher {

    private final KafkaTemplate<String, StudentCreatedEvent> kafkaTemplate;
    private final String topic;

    public StudentEventPublisherKafka(KafkaTemplate<String, StudentCreatedEvent> kafkaTemplate,
                                      @Value("${app.kafka.topic:students.created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishStudentCreated(StudentCreatedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.getId()), event);
    }
}
