package com.example.helloworld.messaging;

import com.example.helloworld.notification.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class StudentDeletedListener {
    private static final Logger log = LoggerFactory.getLogger(StudentDeletedListener.class);

    private final EmailService emailService;
    private final String topic;

    public StudentDeletedListener(EmailService emailService,
                                  @Value("${app.kafka.topic.deleted:students.deleted}") String topic) {
        this.emailService = emailService;
        this.topic = topic;
    }

    @KafkaListener(topics = "#{'${app.kafka.topic.deleted:students.deleted}'}", containerFactory = "kafkaListenerContainerFactoryDeleted")
    public void onStudentDeleted(@Payload StudentDeletedEvent event) {
        log.info("Consumed StudentDeletedEvent: id={}, name={}, email={}", event.getId(), event.getName(), event.getEmail());
        if (event.getEmail() != null) {
            emailService.sendStudentDeletionEmail(event.getEmail(), event.getName(), event.getId());
        } else {
            log.warn("Deleted event missing email, skipping email notification. id={}", event.getId());
        }
    }
}
