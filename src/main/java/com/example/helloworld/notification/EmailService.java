package com.example.helloworld.notification;

public interface EmailService {
    void sendStudentWelcomeEmail(String toEmail, String studentName, Long studentId);
}
