package com.example.helloworld.messaging;

public interface StudentEventPublisher {
    void publishStudentCreated(StudentCreatedEvent event);
    void publishStudentDeleted(StudentDeletedEvent event);
}
