package com.example.helloworld.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class StudentEventPublisherKafka implements StudentEventPublisher {

    private final KafkaTemplate<String, StudentCreatedEvent> createdTemplate;
    private final KafkaTemplate<String, StudentDeletedEvent> deletedTemplate;
    private final String createdTopic;
    private final String deletedTopic;

    public StudentEventPublisherKafka(
            KafkaTemplate<String, StudentCreatedEvent> createdTemplate,
            KafkaTemplate<String, StudentDeletedEvent> deletedTemplate,
            @Value("${app.kafka.topic.created:students.created}") String createdTopic,
            @Value("${app.kafka.topic.deleted:students.deleted}") String deletedTopic) {
        this.createdTemplate = createdTemplate;
        this.deletedTemplate = deletedTemplate;
        this.createdTopic = createdTopic;
        this.deletedTopic = deletedTopic;
    }

    @Override
    public void publishStudentCreated(StudentCreatedEvent event) {
        createdTemplate.send(createdTopic, String.valueOf(event.getId()), event);
    }

    @Override
    public void publishStudentDeleted(StudentDeletedEvent event) {
        deletedTemplate.send(deletedTopic, String.valueOf(event.getId()), event);
    }
}
