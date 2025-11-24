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
public class StudentCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(StudentCreatedListener.class);

    private final EmailService emailService;
    private final String topic;

    public StudentCreatedListener(EmailService emailService,
                                  @Value("${app.kafka.topic.created:students.created}") String topic) {
        this.emailService = emailService;
        this.topic = topic;
    }

    @KafkaListener(topics = "#{'${app.kafka.topic.created:students.created}'}", containerFactory = "kafkaListenerContainerFactory")
    public void onStudentCreated(@Payload StudentCreatedEvent event) {
        log.info("Consumed StudentCreatedEvent: id={}, name={}, email={}", event.getId(), event.getName(), event.getEmail());
        if (event.getEmail() != null) {
            emailService.sendStudentWelcomeEmail(event.getEmail(), event.getName(), event.getId());
        } else {
            log.warn("Event missing email, skipping email notification. id={}", event.getId());
        }
    }
}
