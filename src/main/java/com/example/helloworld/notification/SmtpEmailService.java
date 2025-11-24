package com.example.helloworld.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

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
        // Helpful confirmation in logs so users can see that email dispatch occurred
        log.info("[SMTP] Sent welcome email to {} (id={})", toEmail, studentId);
    }

    @Override
    public void sendStudentDeletionEmail(String toEmail, String studentName, Long studentId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Goodbye, " + studentName);
        message.setText("Your student record with id=" + studentId + " has been deleted.");
        mailSender.send(message);
        log.info("[SMTP] Sent deletion email to {} (id={})", toEmail, studentId);
    }
}
