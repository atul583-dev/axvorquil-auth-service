package com.axvorquil.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    // ── Send Email Verification ────────────────────────────────────
    @Async
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("verificationUrl", baseUrl + "/api/auth/verify-email?token=" + token);
        ctx.setVariable("baseUrl", baseUrl);

        String html = templateEngine.process("email/verification", ctx);
        sendHtmlEmail(toEmail, "Verify your Axvorquil account", html);
        log.info("Verification email sent to: {}", toEmail);
    }

    // ── Send Password Reset ────────────────────────────────────────
    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("resetUrl", baseUrl + "/api/auth/reset-password?token=" + token);
        ctx.setVariable("baseUrl", baseUrl);

        String html = templateEngine.process("email/password-reset", ctx);
        sendHtmlEmail(toEmail, "Reset your Axvorquil password", html);
        log.info("Password reset email sent to: {}", toEmail);
    }

    // ── Send Welcome Email ─────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("loginUrl", baseUrl + "/login");
        ctx.setVariable("baseUrl", baseUrl);

        String html = templateEngine.process("email/welcome", ctx);
        sendHtmlEmail(toEmail, "Welcome to Axvorquil!", html);
        log.info("Welcome email sent to: {}", toEmail);
    }

    // ── Internal send ──────────────────────────────────────────────
    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
