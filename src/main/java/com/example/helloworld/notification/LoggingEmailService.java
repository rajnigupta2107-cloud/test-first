package com.example.helloworld.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(SmtpEmailService.class)
public class LoggingEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendStudentWelcomeEmail(String toEmail, String studentName, Long studentId) {
        log.info("[NO-SMTP] Would send welcome email to {} (id={}): Welcome, {}!", toEmail, studentId, studentName);
    }
}
