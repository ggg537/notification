package com.a.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String token) {
        String link = baseUrl + "/api/auth/verify-email?token=" + token;
        String subject = "Verify your email - SNS Platform";
        String body = "Welcome to SNS Platform!\n\n"
            + "Please verify your email by clicking the link below:\n"
            + link + "\n\n"
            + "This link expires in 24 hours.\n\n"
            + "If you didn't create an account, you can ignore this email.";

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        String subject = "Reset your password - SNS Platform";
        String body = "You requested a password reset.\n\n"
            + "Click the link below to set a new password:\n"
            + link + "\n\n"
            + "This link expires in 1 hour.\n\n"
            + "If you didn't request this, you can ignore this email.";

        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("SMTP not configured. Email would be sent to: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body:\n{}", body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}
