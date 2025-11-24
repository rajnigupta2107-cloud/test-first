package com.example.helloworld.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class StudentEventPublisherNoop implements StudentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(StudentEventPublisherNoop.class);

    @Override
    public void publishStudentCreated(StudentCreatedEvent event) {
        // Log only; useful for local/dev when Kafka is not configured
        log.info("[NO-KAFKA] Student created event: id={}, name={}, email={}",
                event.getId(), event.getName(), event.getEmail());
    }

    @Override
    public void publishStudentDeleted(StudentDeletedEvent event) {
        log.info("[NO-KAFKA] Student deleted event: id={}, name={}, email={}",
                event.getId(), event.getName(), event.getEmail());
    }
}
