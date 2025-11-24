package com.example.helloworld.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendStudentWelcomeEmail(String toEmail, String studentName, Long studentId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome, " + studentName + "!");
        message.setText("Your student account was created with id=" + studentId + ". Welcome aboard!");
        mailSender.send(message);
    }
}
